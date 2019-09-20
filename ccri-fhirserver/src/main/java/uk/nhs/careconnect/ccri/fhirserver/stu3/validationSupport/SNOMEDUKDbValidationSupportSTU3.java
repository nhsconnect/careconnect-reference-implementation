package uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.UriParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class SNOMEDUKDbValidationSupportSTU3 implements IValidationSupport {

  private static final String URL_PREFIX_VALUE_SET = "https://fhir.hl7.org.uk/STU3/ValueSet/";
  private static final String URL_PREFIX_STRUCTURE_DEFINITION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/";
  private static final String URL_PREFIX_STRUCTURE_DEFINITION_BASE = "https://fhir.hl7.org.uk/STU3/";

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SNOMEDUKDbValidationSupportSTU3.class);

  private Map<String, CodeSystem> myCodeSystems;
  private Map<String, StructureDefinition> myStructureDefinitions;
  private Map<String, ValueSet> myValueSets;


  private FhirContext ctxStu3 = null;

  IGenericClient client;

  private IParser parserStu3;

  private String terminologyServer;

  private void logD(String message) {
      log.debug(message);
   //   System.out.println(message);
  }

    private void logW(String message) {
        log.warn(message);
      //  System.out.println(message);
    }

  private void logT(String message) {
    log.trace(message);
    //System.out.println(message);
  }

  public SNOMEDUKDbValidationSupportSTU3(FhirContext stu3Ctx) {

    this.ctxStu3 = stu3Ctx;


    parserStu3 = ctxStu3.newXmlParser();
    this.terminologyServer = HapiProperties.getTerminologyServer();
    try {
      client = this.ctxStu3.newRestfulGenericClient(terminologyServer);
    } catch (Exception ex) {
      log.error(ex.getMessage());
    }
  }
  @Override
  public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fhirContext, ValueSet.ConceptSetComponent conceptSetComponent) {
    if (conceptSetComponent.getValueSet() != null) {
      logW("SNOMED expandValueSet ValueSet="+ conceptSetComponent.getValueSet().toString());
    } else {
      logW("SNOMED expandValueSet System="+ conceptSetComponent.getSystem());
    }
    System.out.println("SNOMED expandValueSet");
    ValueSet.ValueSetExpansionComponent expand = null;

    for (ValueSet.ConceptSetFilterComponent filter : conceptSetComponent.getFilter()) {
      if (filter.hasOp()) {
        log.info("has Filter");
        org.hl7.fhir.dstu3.model.ValueSet vsExpansion = null;
        switch (filter.getOp()) {
          case IN:
            log.info("IN Filter detected");

            vsExpansion =  client
                    .operation()
                    .onType(org.hl7.fhir.dstu3.model.ValueSet.class)
                    .named("expand")
                    .withSearchParameter(org.hl7.fhir.dstu3.model.Parameters.class,"identifier", new UriParam(HapiProperties.getSnomedVersionUrl()+"?fhir_vs=refset/"+filter.getValue()))
                    .returnResourceType(org.hl7.fhir.dstu3.model.ValueSet.class)
                    .useHttpGet()
                    .execute();

            break;
          case EQUAL:
            log.info("EQUAL Filter detected - "+filter.getValue());
            String url = HapiProperties.getSnomedVersionUrl()+"?fhir_vs=ecl/"+filter.getValue();
            //url = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
            log.info(url);
            url = url.replace("^","%5E");
            url = url.replace("|","%7C");
            url = url.replace("<","%3C");
            vsExpansion =  client
                    .operation()
                    .onType(ValueSet.class)
                    .named("expand")
                    .withSearchParameter(Parameters.class,"identifier", new UriParam(url))
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
    //logD("SNOMEDValidator fetchCodeSystem "+theSystem);
    return (CodeSystem) fetchCodeSystemOrValueSet(theContext, theSystem, true);
  }

  private DomainResource fetchCodeSystemOrValueSet(FhirContext theContext, String theSystem, boolean codeSystem) {
    synchronized (this) {
      if (codeSystem) {
        logD("SNOMEDValidator fetch[CodeSystem]OrValueSet: system="+theSystem);
      } else {
        logD("SNOMEDValidator fetchCodeSystemOr[ValueSet]: valueSet="+theSystem);
      }

      Map<String, CodeSystem> codeSystems = myCodeSystems;
      Map<String, ValueSet> valueSets = myValueSets;
      if (codeSystems == null || valueSets == null) {
        codeSystems = new HashMap<String, CodeSystem>();
        valueSets = new HashMap<String, ValueSet>();
        myCodeSystems = codeSystems;
        myValueSets = valueSets;
      }

      if (theSystem.equals(CareConnectSystem.SNOMEDCT) && codeSystem) {
        if (codeSystems.get(theSystem) == null) {
          CodeSystem SNOMEDSystem = new CodeSystem();
          SNOMEDSystem.setStatus(Enumerations.PublicationStatus.ACTIVE);
          SNOMEDSystem.setUrl(CareConnectSystem.SNOMEDCT);
          codeSystems.put(CareConnectSystem.SNOMEDCT, SNOMEDSystem);
        }
      }


      if (codeSystem) {
        return codeSystems.get(theSystem);
      } else {
        return valueSets.get(theSystem);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IBaseResource> T fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {
    Validate.notBlank(theUri, "theUri must not be null or blank");

    if (theClass.equals(StructureDefinition.class)) {
      return (T) fetchStructureDefinition(theContext, theUri);
    }

    if (theClass.equals(ValueSet.class) || theUri.startsWith(URL_PREFIX_VALUE_SET)) {
      return (T) fetchValueSet(theContext, theUri);
    }

    return null;
  }

  @Override
  public StructureDefinition fetchStructureDefinition(FhirContext theContext, String theUrl) {
    String url = theUrl;
    if (url.startsWith(URL_PREFIX_STRUCTURE_DEFINITION)) {
      // no change
    } else if (url.indexOf('/') == -1) {
      url = URL_PREFIX_STRUCTURE_DEFINITION + url;
    } else if (StringUtils.countMatches(url, '/') == 1) {
      url = URL_PREFIX_STRUCTURE_DEFINITION_BASE + url;
    }
    return provideStructureDefinitionMap(theContext).get(url);
  }

  public ValueSet fetchValueSet(FhirContext theContext, String theSystem) {
    return (ValueSet) fetchCodeSystemOrValueSet(theContext, theSystem, false);
  }

  public void flush() {
    myCodeSystems = null;
    myStructureDefinitions = null;
  }

  @Override
  public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
    CodeSystem cs = fetchCodeSystem(theContext, theSystem);
    return cs != null && cs.getContent() != CodeSystem.CodeSystemContentMode.NOTPRESENT;
  }



  private Map<String, StructureDefinition> provideStructureDefinitionMap(FhirContext theContext) {
    Map<String, StructureDefinition> structureDefinitions = myStructureDefinitions;
    if (structureDefinitions == null) {
      structureDefinitions = new HashMap<String, StructureDefinition>();

      myStructureDefinitions = structureDefinitions;
    }
    return structureDefinitions;
  }

  private CodeValidationResult testIfConceptIsInList(String theCode, List<CodeSystem.ConceptDefinitionComponent> conceptList, boolean theCaseSensitive) {
    logD("SNOMEDValidator testIfConceptIsInList: {} code="+ theCode);


    String code = theCode;
    if (theCaseSensitive == false) {
      code = code.toUpperCase();
    }

    return testIfConceptIsInListInner(conceptList, theCaseSensitive, code);
  }

  private CodeValidationResult testIfConceptIsInListInner(List<CodeSystem.ConceptDefinitionComponent> conceptList, boolean theCaseSensitive, String code) {
    logD("SNOMEDValidator testIfConceptIsInListInner: code=" + code);

    /* This is a mock and we will do a basic check (is the code Numeric!
    return positive if numeric else false */


    CodeValidationResult retVal = null;

      if (isNumeric(code)) {


          org.hl7.fhir.dstu3.model.Parameters params = new org.hl7.fhir.dstu3.model.Parameters();
            params.addParameter(
                    new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                            new org.hl7.fhir.dstu3.model.StringType("url"))
                            .setValue(new org.hl7.fhir.dstu3.model.StringType("http://snomed.info/sct?fhir_vs")));
          params.addParameter(
                  new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                          new org.hl7.fhir.dstu3.model.StringType("code"))
                          .setValue(new org.hl7.fhir.dstu3.model.StringType(code)));
          params.addParameter(
                  new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent(
                          new org.hl7.fhir.dstu3.model.StringType("system"))
                          .setValue(new org.hl7.fhir.dstu3.model.StringType("http://snomed.info/sct")));


          org.hl7.fhir.dstu3.model.Parameters paramResult =  client
                  .operation()
                  .onType(org.hl7.fhir.dstu3.model.ValueSet.class)
                  .named("validate-code")
                  .withParameters(params)
                  .returnResourceType(org.hl7.fhir.dstu3.model.Parameters.class)
                  .useHttpGet()
                  .execute();
          logT(ctxStu3.newJsonParser().setPrettyPrint(true).encodeResourceToString(paramResult));
          if (paramResult != null) {
             for(org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent param : paramResult.getParameter()) {
                if (param.getName().equals("result"))  {
                   if (param.getValue() instanceof org.hl7.fhir.dstu3.model.BooleanType) {
                     org.hl7.fhir.dstu3.model.BooleanType bool = (org.hl7.fhir.dstu3.model.BooleanType) param.getValue();
                     if (bool.booleanValue()) {
                       CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                       concept.setCode(code);
                       retVal = new CodeValidationResult(concept);
                     }
                   }

                }
             }
          }
      }
    /* Ignore the list for now KGM Dec 2017 TODO
    for (ConceptDefinitionComponent next : conceptList) {
      // KGM
      logD("SNOMEDValidator testIfConceptIsInListInner NextCode = "+next.getCode());
      String nextCandidate = next.getCode();



    }
    */

    return retVal;
  }

  @Override
  public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay) {
    CodeSystem cs = fetchCodeSystem(theContext, theCodeSystem);
    logD("SNOMEDValidator validateCode system = "+ theCodeSystem);

    if (cs != null) {
      boolean caseSensitive = true;
      if (cs.hasCaseSensitive()) {
        caseSensitive = cs.getCaseSensitive();
      }

      CodeValidationResult retVal = testIfConceptIsInList(theCode, cs.getConcept(), caseSensitive);

      if (retVal != null) {
        return retVal;
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
    return null;
  }
}
