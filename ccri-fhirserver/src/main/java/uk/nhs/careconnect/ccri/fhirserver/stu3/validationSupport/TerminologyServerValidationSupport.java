package uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.UriParam;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminologyServerValidationSupport implements IValidationSupport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TerminologyServerValidationSupport.class);

    public static final String SNOMEDCT = "http://snomed.info/sct";

    private Map<String, CodeSystem> myCodeSystems;
    private Map<String, ValueSet> myValueSets;
    private List<String> notSupportedValueSet;
    private List<String> notSupportedCodeSystem;
    FhirContext stu3ctx;
    IGenericClient client;

    public TerminologyServerValidationSupport(FhirContext stu3ctx, String terminologyUri) throws Exception {
        LOG.info("IG Validation Support Constructor");

        LOG.info("Creating Terminology Server Client {}",terminologyUri);

        this.stu3ctx = stu3ctx;
        this.myCodeSystems = new HashMap();
        this.myValueSets = new HashMap<>();
        notSupportedValueSet = new ArrayList<>();
        notSupportedCodeSystem = new ArrayList<>();
        try {
            client = this.stu3ctx.newRestfulGenericClient(terminologyUri);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw new ServerException(ex.getMessage());
        }
    }

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fhirContext, ValueSet.ConceptSetComponent conceptSetComponent) {
        if (conceptSetComponent.hasValueSet()) {
            LOG.info("SNOMED expandValueSet ValueSet=" + conceptSetComponent.getValueSet().get(0).getValue());
        } else {
            LOG.info("SNOMED expandValueSet System=" + conceptSetComponent.getSystem());
        }

        ValueSet.ValueSetExpansionComponent expand = null;

        for (ValueSet.ConceptSetFilterComponent filter : conceptSetComponent.getFilter()) {
            if (filter.hasOp()) {

                ValueSet vsExpansion = null;
                switch (filter.getOp()) {
                    case IN:
                        LOG.debug("IN Filter detected - "+filter.getValue());
                        vsExpansion = client
                                .operation()
                                .onType(ValueSet.class)
                                .named("expand")
                                .withSearchParameter(Parameters.class, "identifier", new UriParam(HapiProperties.getSnomedVersionUrl() + "?fhir_vs=refset/" + filter.getValue()))
                                .returnResourceType(ValueSet.class)
                                .useHttpGet()
                                .execute();
                        break;
                    case EQUAL:
                        LOG.info("EQUAL Filter detected - " + filter.getValue());
                        String url = HapiProperties.getSnomedVersionUrl() + "?fhir_vs=ecl/" + filter.getValue();
                        //url = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
                        LOG.info(url);
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

                    LOG.debug("EXPANSION RETURNED");
                    expand = vsExpansion.getExpansion();
                    LOG.trace(stu3ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(vsExpansion));
                }
            }
        }
        return expand;
    }

    @Override
    public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext fhirContext) {
        return null;
    }

    @Override
    public CodeSystem fetchCodeSystem(FhirContext fhirContext, String uri) {
        if (notSupportedCodeSystem.contains(uri)) return null;
        if (myCodeSystems.get(uri) != null) return myCodeSystems.get(uri);

        CodeSystem codeSystem = fetchCodeSystemCall(uri);
        if (codeSystem != null) {
            myCodeSystems.put(uri,codeSystem);
        } else {
            notSupportedCodeSystem.add(uri);
        }
        return codeSystem;
    }

    @Override
    public ValueSet fetchValueSet(FhirContext fhirContext, String uri) {
        if (notSupportedValueSet.contains(uri)) return null;
        if (myValueSets.get(uri) != null) return myValueSets.get(uri);
        ValueSet valueSet = fetchValueSetCall(uri);
        if (valueSet != null) {
            myValueSets.put(uri,valueSet);
        } else {
            notSupportedValueSet.add(uri);
        }
        return valueSet;
    }

    @Override
    public StructureDefinition fetchStructureDefinition(FhirContext fhirContext, String s) {
        return null;
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext fhirContext, String uri) {
        if (notSupportedCodeSystem.contains(uri)) return false;
        if (myCodeSystems.get(uri) != null) return true;
        if (fetchCodeSystem(fhirContext,uri) != null) return true;
        return false;
    }

    @Override
    public StructureDefinition generateSnapshot(StructureDefinition structureDefinition, String s, String s1) {
        return null;
    }

    @Override
    public List<IBaseResource> fetchAllConformanceResources(FhirContext fhirContext) {
        ArrayList<IBaseResource> retVal = new ArrayList();
        retVal.addAll(this.myCodeSystems.values());
        retVal.addAll(this.myValueSets.values());
        return retVal;
    }

    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext fhirContext, Class<T> theClass, String theUri) {
        Validate.notBlank(theUri, "theUri must not be null or blank", new Object[0]);
        if (theClass.equals(CodeSystem.class)) {
            return (T) this.fetchCodeSystem(fhirContext, theUri);
        } else if (theClass.equals(ValueSet.class)) {
            return (T) this.fetchValueSet(fhirContext, theUri);
        }
        return null;
    }

    @Override

    public CodeValidationResult validateCode(FhirContext fhirContext, String theCodeSystem, String
        theCode, String theDisplay, String valueSetUri) {
            Parameters params = new Parameters();

            boolean validateCode = true;
            if (valueSetUri != null) {
                LOG.debug("ONTO UK ValidateCode [System " + theCodeSystem + "] [Code=" + theCode + "] [ValueSet="+valueSetUri+"]");
                params.addParameter(
                        new Parameters.ParametersParameterComponent(
                                new StringType("url"))
                                .setValue(new StringType(valueSetUri)));
            } else {
                LOG.debug("ONTO UK ValidateCode [System " + theCodeSystem + "] [Code=" + theCode + "]");
                if (theCodeSystem.equals(SNOMEDCT)) {
                    params.addParameter(
                            new Parameters.ParametersParameterComponent(
                                    new StringType("url"))
                                    .setValue(new StringType(HapiProperties.getSnomedVersionUrl() + "?fhir_vs")));
                } else {
                    validateCode = false;
                }
            }

            // To validate SNOMED we need to use the UK ValueSet else use CodeSystem
            // Unless the CodeSystem isn't supplied
            if (!Constants.codeSystemNotNeeded(theCodeSystem)) {
                params.addParameter(
                        new Parameters.ParametersParameterComponent(
                                new StringType("system"))
                                .setValue(new StringType(theCodeSystem)));
            } else {
                if (valueSetUri != null && !valueSetUri.isEmpty()) {
                    ValueSet vs = fetchValueSetCall(valueSetUri);
                    if (vs != null && vs.hasCompose()) {
                        if (vs.getCompose().hasInclude() && vs.getCompose().getInclude().size() == 1) {
                            if (vs.getCompose().getIncludeFirstRep().hasSystem()) {
                                params.addParameter(
                                        new Parameters.ParametersParameterComponent(
                                                new StringType("system"))
                                                .setValue(new StringType(vs.getCompose().getIncludeFirstRep().getSystem())));
                            }
                        }
                    }
                }
                // validateCode = false;
            }

            if (theDisplay != null) {
                params.addParameter(
                        new Parameters.ParametersParameterComponent(
                                new StringType("display"))
                                .setValue(new StringType(theDisplay)));
            }

            params.addParameter(
                    new Parameters.ParametersParameterComponent(
                            new StringType("code"))
                            .setValue(new StringType(theCode)));

            Parameters paramResult= null;
            if (validateCode) {
                try {
                    paramResult = client
                            .operation()
                            .onType(ValueSet.class)
                            .named("validate-code")
                            .withParameters(params)
                            .returnResourceType(Parameters.class)
                            .useHttpGet()
                            .execute();
                    if (paramResult != null) {
                        String message = null;
                        for (Parameters.ParametersParameterComponent param : paramResult.getParameter()) {
                            if (param.getName().equals("result")) {
                                if (param.getValue() instanceof BooleanType) {
                                    BooleanType bool = (BooleanType) param.getValue();
                                    if (bool.booleanValue()) {
                                        CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                                        concept.setCode(theCode);
                                        return new CodeValidationResult(concept);
                                    }
                                }
                            }
                            if (param.getName().equals("message")) {
                                if (param.getValue() instanceof StringType) {
                                    StringType paramValue = (StringType) param.getValue();
                                    message = paramValue.getValue();
                                }
                            }
                        }
                        if (message != null) {
                            return new CodeValidationResult(ValidationMessage.IssueSeverity.WARNING, message);
                        }
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            } else {
                try {
                    paramResult = client
                            .operation()
                            .onType(CodeSystem.class)
                            .named("lookup")
                            .withParameters(params)
                            .returnResourceType(Parameters.class)
                            .useHttpGet()
                            .execute();
                    if (paramResult != null) {
                        CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                        concept.setCode(theCode);
                        return new CodeValidationResult(concept);
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            }

            return new CodeValidationResult(ValidationMessage.IssueSeverity.WARNING, "SNOMEDValidator Unknown code: " + theCodeSystem + " / " + theCode);
        }

    @Override
    public LookupCodeResult lookupCode(FhirContext fhirContext, String s, String s1) {
        return null;
    }

    private ValueSet fetchValueSetCall( String uri) {
        Bundle bundle = client.search().forResource(ValueSet.class).where(ValueSet.URL.matches().value(uri))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.hasEntry() && bundle.getEntryFirstRep().getResource() instanceof ValueSet) {
            LOG.debug("fetchValueSet OK {}} ",uri);
            return (ValueSet) bundle.getEntryFirstRep().getResource();
        } else {
            LOG.info("fetchValueSet MISSING {} ", uri);
        }

        return null;
    }
    private CodeSystem fetchCodeSystemCall( String uri) {
        Bundle results = null;
        try {
            results = client.search().forResource(CodeSystem.class).where(ValueSet.URL.matches().value(uri))
                    .returnBundle(Bundle.class)
                    .execute();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
        if (results == null) return null;

        if (results.hasEntry() && results.getEntryFirstRep().getResource() instanceof CodeSystem) {
            LOG.debug("fetchCodeSystem OK {}", uri);
            return (CodeSystem) results.getEntryFirstRep().getResource();
        } else {
            LOG.info("fetchCodeSystem MISSING {}", uri);
        }

        return null;
    }
}
