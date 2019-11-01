package uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.UriParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import org.aspectj.apache.bcel.classfile.Code;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class SNOMEDUKDbValidationSupportSTU3 implements IValidationSupport {

    private static final String URL_HL7UK_BASE = "https://fhir.hl7.org.uk/STU3";
    private static final String URL_NHSD_BASE = "https://fhir.nhs.uk/STU3";



    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SNOMEDUKDbValidationSupportSTU3.class);

    private Map<String, CodeSystem> myCodeSystems;
    private Map<String, StructureDefinition> myStructureDefinitions;
    private Map<String, ValueSet> myValueSets;

    private static final int CONNECT_TIMEOUT_MILLIS = 50000;
    private static int SC_OK = 200;
    private static final int READ_TIMEOUT_MILLIS = 50000;

    private FhirContext ctxStu3 = null;

    IGenericClient client;
    IGenericClient clientNHSD;
    IGenericClient clientHL7UK;

    private IParser parserStu3;

    private String terminologyServer;

    private void logD(String message) {
        log.info(message);
    }

    private void logW(String message) {
        log.warn(message);
    }

    private void logT(String message) {
        log.trace(message);
    }

    private void logD(String message, Object value) {
        log.debug(String.format(message, value));
    }

    private void logW(String message, Object value) {
        log.warn(String.format(message, value));
    }


    public SNOMEDUKDbValidationSupportSTU3(FhirContext stu3Ctx) {

        this.ctxStu3 = stu3Ctx;

        myCodeSystems = new HashMap<>();
        myValueSets = new HashMap<>();

        parserStu3 = ctxStu3.newXmlParser();
        this.terminologyServer = HapiProperties.getTerminologyServer();
        try {
            client = this.ctxStu3.newRestfulGenericClient(terminologyServer);
            clientNHSD = this.ctxStu3.newRestfulGenericClient(URL_NHSD_BASE);
            clientHL7UK = this.ctxStu3.newRestfulGenericClient(URL_HL7UK_BASE);

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fhirContext, ValueSet.ConceptSetComponent conceptSetComponent) {
        if (conceptSetComponent.getValueSet() != null) {
            logD("SNOMED expandValueSet ValueSet=" + conceptSetComponent.getValueSet().toString());
        } else {
            logD("SNOMED expandValueSet System=" + conceptSetComponent.getSystem());
        }

        ValueSet.ValueSetExpansionComponent expand = null;

        for (ValueSet.ConceptSetFilterComponent filter : conceptSetComponent.getFilter()) {
            if (filter.hasOp()) {
                log.info("has Filter");
                org.hl7.fhir.dstu3.model.ValueSet vsExpansion = null;
                switch (filter.getOp()) {
                    case IN:
                        log.info("IN Filter detected");

                        vsExpansion = client
                                .operation()
                                .onType(org.hl7.fhir.dstu3.model.ValueSet.class)
                                .named("expand")
                                .withSearchParameter(org.hl7.fhir.dstu3.model.Parameters.class, "identifier", new UriParam(HapiProperties.getSnomedVersionUrl() + "?fhir_vs=refset/" + filter.getValue()))
                                .returnResourceType(org.hl7.fhir.dstu3.model.ValueSet.class)
                                .useHttpGet()
                                .execute();

                        break;
                    case EQUAL:
                        log.info("EQUAL Filter detected - " + filter.getValue());
                        String url = HapiProperties.getSnomedVersionUrl() + "?fhir_vs=ecl/" + filter.getValue();
                        //url = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
                        log.info(url);
                        url = url.replace("^", "%5E");
                        url = url.replace("|", "%7C");
                        url = url.replace("<", "%3C");
                        vsExpansion = client
                                .operation()
                                .onType(ValueSet.class)
                                .named("expand")
                                .withSearchParameter(Parameters.class, "identifier", new UriParam(url))
                                .returnResourceType(ValueSet.class)
                                .useHttpGet()
                                .execute();
                }
                if (vsExpansion != null) {

                    log.debug("EXPANSION RETURNED");
                    expand = vsExpansion.getExpansion();
                    log.trace(ctxStu3.newJsonParser().setPrettyPrint(true).encodeResourceToString(vsExpansion));
                }
            }
        }
        return expand;
    }

    @Override
    public List<IBaseResource> fetchAllConformanceResources(FhirContext theContext) {
        ArrayList<IBaseResource> retVal = new ArrayList<>();
        retVal.addAll(myCodeSystems.values());
        retVal.addAll(myStructureDefinitions.values());
        retVal.addAll(myValueSets.values());
        return retVal;
    }


    @Override
    public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext theContext) {
        return new ArrayList<StructureDefinition>(provideStructureDefinitionMap(theContext).values());
    }

    @Override
    public CodeSystem fetchCodeSystem(FhirContext theContext, String theSystem) {
        logD("SNOMEDValidator fetchCodeSystem "+theSystem);
        return (CodeSystem) fetchCodeSystemOrValueSet(theContext, theSystem, true);
    }

    private DomainResource fetchCodeSystemOrValueSet(FhirContext theContext, String theSystem, boolean codeSystem) {
        synchronized (this) {

            if (codeSystem) {
                logD("SNOMEDValidator fetch[CodeSystem]OrValueSet: system=" + theSystem);
            } else {
                logD("SNOMEDValidator fetchCodeSystemOr[ValueSet]: valueSet=" + theSystem);
            }

            if (codeSystem) {

                if (myCodeSystems.get(theSystem) != null) return myCodeSystems.get(theSystem);

                CodeSystem cs = (CodeSystem) fetchCodeSystemCall(client,theSystem);

                if (cs != null) {
                    myCodeSystems.put(theSystem, cs);
                    return cs;
                }

            } else {
                logD("Request for ValueSet {}",theSystem);
                if (myValueSets.get(theSystem) != null) return myValueSets.get(theSystem);
            }

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IBaseResource> T
    fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {
        logD("SNOMEDValidator fetchResource "+theUri);
        if (theUri.contains("CodeSystem") || theUri.equals(CareConnectSystem.SNOMEDCT)) {
            return (T) fetchCodeSystemCall(client,theUri);
        } else {
            return (T) fetchValueSetCall(client, theUri);
        }
    }

    private IBaseResource fetchValueSetCall(IGenericClient client, String uri) {
        ValueSet resource = fetchValueSetCallInner(client, uri);
        if (resource == null)  {
            if (uri.startsWith(URL_HL7UK_BASE + "/ValueSet")) resource = fetchValueSetCallInner(clientHL7UK,uri);
            if (uri.startsWith(URL_NHSD_BASE + "/ValueSet")) resource = fetchValueSetCallInner(clientNHSD,uri);
            if (resource != null) {
                resource.setId("");
                client.create().resource(resource).execute();
            }
        }
        return resource;
    }

    private ValueSet fetchValueSetCallInner(IGenericClient client, String uri) {
        Bundle bundle = client.search().forResource(ValueSet.class).where(ValueSet.URL.matches().value(uri))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.hasEntry() && bundle.getEntryFirstRep().getResource() instanceof ValueSet) {
            logD("fetchValueSet OK " + uri + " server = "+client.getServerBase());
            return (ValueSet) bundle.getEntryFirstRep().getResource();
        } else {
            logD("fetchValueSet MISSING " + uri + " server = "+client.getServerBase());
        }

        return null;
    }

    private CodeSystem fetchCodeSystemCall(IGenericClient client, String uri) {
        CodeSystem resource = fetchCodeSystemCallInner(client,uri);
        if (resource == null)  {
            if (uri.startsWith(URL_HL7UK_BASE + "/CodeSystem")) resource = fetchCodeSystemCallInner(clientHL7UK,uri);
            if (uri.startsWith(URL_NHSD_BASE + "/System"))   resource = fetchCodeSystemCallInner(clientNHSD,uri);
            if (resource != null) {
                resource.setId("");
                client.create().resource(resource).execute();
            }
        }
        return resource;
    }

    private CodeSystem fetchCodeSystemCallInner(IGenericClient client, String uri) {
        Bundle results = null;
        if (uri.equals(CareConnectSystem.SNOMEDCT)) {
            results = client.search().forResource(CodeSystem.class).where(ValueSet.URL.matches().value(uri))
                    .returnBundle(Bundle.class)
                    .execute();
        } else {

            results = client.search().forResource(CodeSystem.class)
                    .where(CodeSystem.VERSION.exactly().code(HapiProperties.getSnomedVersionUrl()))
                    .and(CodeSystem.URL.matches().value(CareConnectSystem.SNOMEDCT))
                    .returnBundle(Bundle.class)
                    .execute();
        }
        if (results==null) return null;

        if (results.hasEntry() && results.getEntryFirstRep().getResource() instanceof CodeSystem) {
            logD("fetchCodeSystem OK " + uri);
            return (CodeSystem) results.getEntryFirstRep().getResource();
        } else {
            logD("fetchCodeSystem MISSING " + uri);
        }

        return null;
    }

    @Override
    public StructureDefinition fetchStructureDefinition(FhirContext theContext, String theUrl) {
        return null;
    }

    @Override
    public ValueSet fetchValueSet(FhirContext theContext, String theSystem) {
        logD("SNOMEDValidator fetchValueSet "+theSystem);
        return (ValueSet) fetchCodeSystemOrValueSet(theContext, theSystem, false);
    }


    @Override
    public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
        logD("SNOMEDValidator isCodeSystemSupported "+theSystem);
        if (theSystem.equals(CareConnectSystem.SNOMEDCT)) return true;
        if (theSystem.startsWith(URL_NHSD_BASE)) return true;
        if (theSystem.startsWith(URL_HL7UK_BASE)) return true;
        return false;
    }


    private Map<String, StructureDefinition> provideStructureDefinitionMap(FhirContext theContext) {
        Map<String, StructureDefinition> structureDefinitions = myStructureDefinitions;
        if (structureDefinitions == null) {
            structureDefinitions = new HashMap<String, StructureDefinition>();

            myStructureDefinitions = structureDefinitions;
        }
        return structureDefinitions;
    }


    @Override
    public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String
            theCode, String theDisplay) {

        org.hl7.fhir.dstu3.model.Parameters params = new org.hl7.fhir.dstu3.model.Parameters();
        if (theCodeSystem.equals(CareConnectSystem.SNOMEDCT)) {
            params.addParameter(
                    new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                            new org.hl7.fhir.dstu3.model.StringType("url"))
                            .setValue(new org.hl7.fhir.dstu3.model.StringType(HapiProperties.getSnomedVersionUrl() + "?fhir_vs")));
            params.addParameter(
                    new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                            new org.hl7.fhir.dstu3.model.StringType("system"))
                            .setValue(new org.hl7.fhir.dstu3.model.StringType(theCodeSystem)));

        } else {
            params.addParameter(
                    new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                            new org.hl7.fhir.dstu3.model.StringType("url"))
                            .setValue(new org.hl7.fhir.dstu3.model.StringType(theCodeSystem)));
        }
        params.addParameter(
                new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                        new org.hl7.fhir.dstu3.model.StringType("code"))
                        .setValue(new org.hl7.fhir.dstu3.model.StringType(theCode)));

        org.hl7.fhir.dstu3.model.Parameters paramResult = client
                .operation()
                .onType(org.hl7.fhir.dstu3.model.ValueSet.class)
                .named("validate-code")
                .withParameters(params)
                .returnResourceType(org.hl7.fhir.dstu3.model.Parameters.class)
                .useHttpGet()
                .execute();

        logD(ctxStu3.newJsonParser().setPrettyPrint(true).encodeResourceToString(paramResult));
        if (paramResult != null) {
            for (org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent param : paramResult.getParameter()) {
                if (param.getName().equals("result")) {
                    if (param.getValue() instanceof org.hl7.fhir.dstu3.model.BooleanType) {
                        org.hl7.fhir.dstu3.model.BooleanType bool = (org.hl7.fhir.dstu3.model.BooleanType) param.getValue();
                        if (bool.booleanValue()) {
                            CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                            concept.setCode(theCode);
                            return new CodeValidationResult(concept);
                        }
                    }

                }
            }
        }

        return new CodeValidationResult(IssueSeverity.WARNING, "SNOMEDValidator Unknown code: " + theCodeSystem + " / " + theCode);
    }

    @Override
    public StructureDefinition generateSnapshot(StructureDefinition structureDefinition, String s, String s1) {
        return null;
    }

    @Override
    public LookupCodeResult lookupCode(FhirContext fhirContext, String s, String s1) {
        logD("SNOMEDValidator lookupCode "+s+ " " + s1);
        return null;
    }
}
