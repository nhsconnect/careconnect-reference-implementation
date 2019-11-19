package uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;
import ca.uhn.fhir.rest.api.Constants;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class SNOMEDUKDbValidationSupportSTU3 implements IValidationSupport {

    private static final String URL_HL7UK_BASE = "https://fhir.hl7.org.uk/STU3";
    private static final String URL_NHSD_BASE = "https://fhir.nhs.uk/STU3";


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SNOMEDUKDbValidationSupportSTU3.class);

    private Map<String, CodeSystem> myCodeSystems;
    private Map<String, StructureDefinition> myStructureDefinitions;
    private Map<String, ValueSet> myValueSets;
    private List<String> notSupportedValueSet;
    private List<String> notSupportedCodeSystem;

    private static final int CONNECT_TIMEOUT_MILLIS = 50000;
    private static int SC_OK = 200;
    private static final int READ_TIMEOUT_MILLIS = 50000;

    private FhirContext ctxStu3 = null;

    IGenericClient client;
    IGenericClient clientNHSD;
    IGenericClient clientHL7UK;

    private IParser parserStu3;

    private String terminologyServer;

    private void logI(String message) {
        log.info(message);
    }

    private void logD(String message) {
        log.info(message);
    }

    private void logW(String message) {
        log.warn(message);
    }

    private void logT(String message) {
        log.trace(message);
    }



    public SNOMEDUKDbValidationSupportSTU3(FhirContext stu3Ctx) {

        this.ctxStu3 = stu3Ctx;

        myCodeSystems = new HashMap<>();
        myValueSets = new HashMap<>();
        notSupportedValueSet = new ArrayList<>();
        notSupportedCodeSystem = new ArrayList<>();

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
        if (conceptSetComponent.hasValueSet()) {
            logI("SNOMED expandValueSet ValueSet=" + conceptSetComponent.getValueSet().get(0).getValue());
        } else {
            logI("SNOMED expandValueSet System=" + conceptSetComponent.getSystem());
        }

        ValueSet.ValueSetExpansionComponent expand = null;

        for (ValueSet.ConceptSetFilterComponent filter : conceptSetComponent.getFilter()) {
            if (filter.hasOp()) {

                org.hl7.fhir.dstu3.model.ValueSet vsExpansion = null;
                switch (filter.getOp()) {
                    case IN:
                        logI("IN Filter detected - "+filter.getValue());
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
    public ValueSet fetchValueSet(FhirContext theContext, String theSystem) {
        logD("SNOMEDValidator fetchValueSet " + theSystem);
        return (ValueSet) fetchCodeSystemOrValueSet(theContext, theSystem, false);
    }


    @Override
    public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
        logD("SNOMEDValidator isCodeSystemSupported " + theSystem);
        if (isBlank(theSystem) || Constants.codeSystemNotNeeded(theSystem)) {
            return false;
        }
        if (theSystem.equals(CareConnectSystem.SNOMEDCT)) return true;
        if (myCodeSystems.get(theSystem) != null) return true;
        if (notSupportedCodeSystem.contains(theSystem)) return false;
        IBaseResource resource = fetchCodeSystem(theContext, theSystem);
        if (resource != null) {
            //myCodeSystems.put(theSystem, (CodeSystem) resource);
            return true;
        }

        return false;
    }

    @Override
    public CodeSystem fetchCodeSystem(FhirContext theContext, String theSystem) {
        logD("SNOMEDValidator fetchCodeSystem " + theSystem);
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
                CodeSystem cs = (CodeSystem) fetchCodeSystemCall(client, theSystem);
                if (cs != null) {
                  //  myCodeSystems.put(theSystem, cs);
                    return cs;
                }

            } else {
                if (myValueSets.get(theSystem) != null) return myValueSets.get(theSystem);
                ValueSet vs = (ValueSet) fetchValueSetCall(theContext, client, theSystem);
                if (vs != null) {
                  //  myValueSets.put(theSystem, vs);
                    return vs;
                }
            }

        }
        return null;
    }

    @Override
    public CodeValidationResult validateCode(FhirContext fhirContext, String theCodeSystem, String
            theCode, String theDisplay, String valueSetUri) {
        org.hl7.fhir.dstu3.model.Parameters params = new org.hl7.fhir.dstu3.model.Parameters();

        boolean validateCode = true;
        if (valueSetUri != null) {
            logD("SNOMED UK ValidateCode [System " + theCodeSystem + "] [Code=" + theCode + "] [ValueSet="+valueSetUri+"]");
            params.addParameter(
                    new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                            new org.hl7.fhir.dstu3.model.StringType("url"))
                            .setValue(new org.hl7.fhir.dstu3.model.StringType(valueSetUri)));
        } else {
            logD("SNOMED UK ValidateCode [System " + theCodeSystem + "] [Code=" + theCode + "]");
            if (theCodeSystem.equals(CareConnectSystem.SNOMEDCT)) {
                params.addParameter(
                        new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                                new org.hl7.fhir.dstu3.model.StringType("url"))
                                .setValue(new org.hl7.fhir.dstu3.model.StringType(HapiProperties.getSnomedVersionUrl() + "?fhir_vs")));
            } else {
                validateCode = false;
            }
        }

        // To validate SNOMED we need to use the UK ValueSet else use CodeSystem

        params.addParameter(
                new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                        new org.hl7.fhir.dstu3.model.StringType("system"))
                        .setValue(new org.hl7.fhir.dstu3.model.StringType(theCodeSystem)));

        if (theDisplay != null) {
            params.addParameter(
                    new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                            new org.hl7.fhir.dstu3.model.StringType("display"))
                            .setValue(new org.hl7.fhir.dstu3.model.StringType(theDisplay)));
        }

        params.addParameter(
                new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                        new org.hl7.fhir.dstu3.model.StringType("code"))
                        .setValue(new org.hl7.fhir.dstu3.model.StringType(theCode)));

        org.hl7.fhir.dstu3.model.Parameters paramResult= null;
        if (validateCode) {
            try {
                paramResult = client
                        .operation()
                        .onType(org.hl7.fhir.dstu3.model.ValueSet.class)
                        .named("validate-code")
                        .withParameters(params)
                        .returnResourceType(org.hl7.fhir.dstu3.model.Parameters.class)
                        .useHttpGet()
                        .execute();
                if (paramResult != null) {
                    String message = null;
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
                        if (param.getName().equals("message")) {
                            if (param.getValue() instanceof org.hl7.fhir.dstu3.model.StringType) {
                                org.hl7.fhir.dstu3.model.StringType paramValue = (org.hl7.fhir.dstu3.model.StringType) param.getValue();
                                message = paramValue.getValue();
                            }
                        }
                    }
                    if (message != null) {
                        return new CodeValidationResult(IssueSeverity.WARNING, message);
                    }
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        } else {
            try {
                paramResult = client
                        .operation()
                        .onType(org.hl7.fhir.dstu3.model.CodeSystem.class)
                        .named("lookup")
                        .withParameters(params)
                        .returnResourceType(org.hl7.fhir.dstu3.model.Parameters.class)
                        .useHttpGet()
                        .execute();
                if (paramResult != null) {
                    CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                    concept.setCode(theCode);
                    return new CodeValidationResult(concept);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }




        return new CodeValidationResult(IssueSeverity.WARNING, "SNOMEDValidator Unknown code: " + theCodeSystem + " / " + theCode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IBaseResource> T
    fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {
        logD("SNOMEDValidator fetchResource " + theUri);
        if (theUri.contains("CodeSystem") || theUri.equals(CareConnectSystem.SNOMEDCT)) {
            return (T) fetchCodeSystemCall(client, theUri);
        } else if (theUri.contains("ValueSet") ) {
            return (T) fetchValueSetCall(theContext, client, theUri);
        }
        return null;
    }

    private ValueSet fetchValueSetCall(FhirContext theContext, IGenericClient client, String uri) {

        if (notSupportedValueSet.contains(uri)) return null;
        if (myValueSets.get(uri) != null) return myValueSets.get(uri);

        ValueSet resource = fetchValueSetCallInner(client, uri);
        if (resource == null) {

            if (uri.startsWith(URL_HL7UK_BASE + "/ValueSet")) resource = fetchValueSetCallInner(clientHL7UK, uri);
            if (uri.startsWith(URL_NHSD_BASE + "/ValueSet")) resource = fetchValueSetCallInner(clientNHSD, uri);
            if (resource != null) {
                ValueSet valueSet = (ValueSet) resource;
                if (valueSet.hasCompose() && valueSet.getCompose().hasInclude()) {
                    boolean hasAllCodeSystems = true;
                    for (ValueSet.ConceptSetComponent include : valueSet.getCompose().getInclude()) {
                        if (include.hasSystem() && !isCodeSystemSupported(theContext, include.getSystem())) hasAllCodeSystems = true;
                    }

                    if (hasAllCodeSystems) {
                        logI("Updating OntoServer " + uri);
                        try {
                            MethodOutcome method = client.create()
                                    .resource(valueSet)
                                    .conditional().where(ValueSet.URL.matches().value(valueSet.getUrl()))
                                    .execute();
                            if (method.getCreated()) {
                                logI("Ontology server. Create ValueSet " + uri);
                            }
                        } catch (Exception ex) {
                            log.error(ex.getMessage());
                            resource = null;
                            notSupportedValueSet.add(uri);
                        }
                    }

                }
            }
        }
        if (resource != null) myValueSets.put(uri,resource);
        return resource;
    }

    private ValueSet fetchValueSetCallInner(IGenericClient client, String uri) {
        Bundle bundle = client.search().forResource(ValueSet.class).where(ValueSet.URL.matches().value(uri))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.hasEntry() && bundle.getEntryFirstRep().getResource() instanceof ValueSet) {
            logD("fetchValueSet OK " + uri + " server = " + client.getServerBase());
            return (ValueSet) bundle.getEntryFirstRep().getResource();
        } else {
            logD("fetchValueSet MISSING " + uri + " server = " + client.getServerBase());
        }

        return null;
    }

    private CodeSystem fetchCodeSystemCall(IGenericClient client, String uri) {

        if (notSupportedCodeSystem.contains(uri)) return null;
        if (myCodeSystems.get(uri) != null) return myCodeSystems.get(uri);

        CodeSystem resource = fetchCodeSystemCallInner(client, uri);

        // NHSD Termserver doesn't support adding CodeSystems??
        if (resource == null) {
            if (uri.startsWith(URL_HL7UK_BASE + "/CodeSystem")) resource = fetchCodeSystemCallInner(clientHL7UK, uri);
            if (uri.startsWith(URL_NHSD_BASE + "/CodeSystem")) resource = fetchCodeSystemCallInner(clientNHSD, uri);
            if (resource != null) {
                resource.setId("");
                try {
                    MethodOutcome method = client.create()
                            .resource(resource)
                            .conditional().where(CodeSystem.URL.matches().value(resource.getUrl()))
                            .execute();
                    if (method.getCreated()) {
                        logI("Ontology server. Create CodeSystem " + uri);

                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }
        if (resource != null) myCodeSystems.put(uri,resource);

        return resource;
    }

    private CodeSystem fetchCodeSystemCallInner(IGenericClient client, String uri) {
        Bundle results = null;
        try {
            results = client.search().forResource(CodeSystem.class).where(ValueSet.URL.matches().value(uri))
                    .returnBundle(Bundle.class)
                    .execute();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        if (results == null) return null;

        if (results.hasEntry() && results.getEntryFirstRep().getResource() instanceof CodeSystem) {
            logD("fetchCodeSystem OK " + uri);
            return (CodeSystem) results.getEntryFirstRep().getResource();
        } else {
            logI("fetchCodeSystem MISSING " + uri);
        }

        return null;
    }

    @Override
    public StructureDefinition fetchStructureDefinition(FhirContext theContext, String theUrl) {
        return null;
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
    public StructureDefinition generateSnapshot(StructureDefinition structureDefinition, String s, String s1) {
        return null;
    }

    @Override
    public LookupCodeResult lookupCode(FhirContext fhirContext, String s, String s1) {
        logD("SNOMEDValidator lookupCode " + s + " " + s1);
        return null;
    }
}
