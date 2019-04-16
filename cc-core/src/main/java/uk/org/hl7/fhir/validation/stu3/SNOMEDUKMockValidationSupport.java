package uk.org.hl7.fhir.validation.stu3;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.dstu3.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class SNOMEDUKMockValidationSupport implements IValidationSupport {

  private static final String URL_PREFIX_VALUE_SET = "https://fhir.hl7.org.uk/STU3/ValueSet/";
  private static final String URL_PREFIX_STRUCTURE_DEFINITION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/";
  private static final String URL_PREFIX_STRUCTURE_DEFINITION_BASE = "https://fhir.hl7.org.uk/STU3/";

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SNOMEDUKMockValidationSupport.class);

  private Map<String, CodeSystem> myCodeSystems;
  private Map<String, StructureDefinition> myStructureDefinitions;
  private Map<String, ValueSet> myValueSets;

  private void logD(String message) {
      log.debug(message);
     // System.out.println(message);
  }

    private void logW(String message) {
        log.warn(message);
        System.out.println(message);
    }
  @Override
  public ValueSetExpansionComponent expandValueSet(FhirContext theContext, ConceptSetComponent theInclude) {
   // logD("SNOMED MOCK expandValueSet System="+theInclude.getSystem());
    ValueSetExpansionComponent retVal = new ValueSetExpansionComponent();

    Set<String> wantCodes = new HashSet<String>();
    for (ConceptReferenceComponent next : theInclude.getConcept()) {
      logD("SNOMED MOCK expandValueSet System="+theInclude.getSystem()+" wantCodes.add "+next.getCode());
      wantCodes.add(next.getCode());
    }

    CodeSystem system = fetchCodeSystem(theContext, theInclude.getSystem());
    for (ConceptDefinitionComponent next : system.getConcept()) {
      if (wantCodes.isEmpty() || wantCodes.contains(next.getCode())) {
        retVal.addContains().setSystem(theInclude.getSystem()).setCode(next.getCode()).setDisplay(next.getDisplay());
      }
    }

    return retVal;
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
    //logD("SNOMEDMOCK fetchCodeSystem "+theSystem);
    return (CodeSystem) fetchCodeSystemOrValueSet(theContext, theSystem, true);
  }

  private DomainResource fetchCodeSystemOrValueSet(FhirContext theContext, String theSystem, boolean codeSystem) {
    synchronized (this) {
      logD("SNOMEDMOCK fetchCodeSystemOrValueSet: system="+theSystem);

      Map<String, CodeSystem> codeSystems = myCodeSystems;
      Map<String, ValueSet> valueSets = myValueSets;
      if (codeSystems == null || valueSets == null) {
        codeSystems = new HashMap<String, CodeSystem>();
        valueSets = new HashMap<String, ValueSet>();


          if (theSystem.equals(CareConnectSystem.SNOMEDCT)) {
              
              // Mock SNOMED support TODO point to real SNOMED UK Server
              
              CodeSystem SNOMEDSystem = new CodeSystem();
              SNOMEDSystem.setStatus(Enumerations.PublicationStatus.ACTIVE);
              SNOMEDSystem.setUrl(CareConnectSystem.SNOMEDCT);
              codeSystems.put(CareConnectSystem.SNOMEDCT,SNOMEDSystem);
              
          }
        myCodeSystems = codeSystems;
        myValueSets = valueSets;
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
    return cs != null && cs.getContent() != CodeSystemContentMode.NOTPRESENT;
  }

  private void loadCodeSystems(FhirContext theContext, Map<String, CodeSystem> theCodeSystems, Map<String, ValueSet> theValueSets, String theClasspath) {
    logD("SNOMEDMOCK Loading CodeSystem/ValueSet from classpath: "+ theClasspath);
    InputStream valuesetText = SNOMEDUKMockValidationSupport.class.getResourceAsStream(theClasspath);
    if (valuesetText != null) {
      InputStreamReader reader = new InputStreamReader(valuesetText, Charsets.UTF_8);

      Bundle bundle = theContext.newXmlParser().parseResource(Bundle.class, reader);
      for (BundleEntryComponent next : bundle.getEntry()) {
        if (next.getResource() instanceof CodeSystem) {
          CodeSystem nextValueSet = (CodeSystem) next.getResource();
          nextValueSet.getText().setDivAsString("");
          String system = nextValueSet.getUrl();
          if (isNotBlank(system)) {
            theCodeSystems.put(system, nextValueSet);
          }
        } else if (next.getResource() instanceof ValueSet) {
          ValueSet nextValueSet = (ValueSet) next.getResource();
          nextValueSet.getText().setDivAsString("");
          String system = nextValueSet.getUrl();
          if (isNotBlank(system)) {
            theValueSets.put(system, nextValueSet);
          }
        }
      }
    } else {
      logW("Unable to load resource: "+ theClasspath);

    }
  }

  private void loadStructureDefinitions(FhirContext theContext, Map<String, StructureDefinition> theCodeSystems, String theClasspath) {
    logD("SNOMEDMOCK Loading structure definitions from classpath: "+ theClasspath);
    InputStream valuesetText = SNOMEDUKMockValidationSupport.class.getResourceAsStream(theClasspath);
    if (valuesetText != null) {
      InputStreamReader reader = new InputStreamReader(valuesetText, Charsets.UTF_8);

      Bundle bundle = theContext.newXmlParser().parseResource(Bundle.class, reader);
      for (BundleEntryComponent next : bundle.getEntry()) {
        if (next.getResource() instanceof StructureDefinition) {
          StructureDefinition nextSd = (StructureDefinition) next.getResource();
          nextSd.getText().setDivAsString("");
          String system = nextSd.getUrl();
          if (isNotBlank(system)) {
            theCodeSystems.put(system, nextSd);
          }
        }
      }
    } else {
      log.warn("Unable to load resource: {}", theClasspath);
    }
  }

  private Map<String, StructureDefinition> provideStructureDefinitionMap(FhirContext theContext) {
    Map<String, StructureDefinition> structureDefinitions = myStructureDefinitions;
    if (structureDefinitions == null) {
      structureDefinitions = new HashMap<String, StructureDefinition>();

      myStructureDefinitions = structureDefinitions;
    }
    return structureDefinitions;
  }

  private CodeValidationResult testIfConceptIsInList(String theCode, List<ConceptDefinitionComponent> conceptList, boolean theCaseSensitive) {
    logD("SNOMEDMOCK testIfConceptIsInList: {} code="+ theCode);


    String code = theCode;
    if (theCaseSensitive == false) {
      code = code.toUpperCase();
    }

    return testIfConceptIsInListInner(conceptList, theCaseSensitive, code);
  }

  private CodeValidationResult testIfConceptIsInListInner(List<ConceptDefinitionComponent> conceptList, boolean theCaseSensitive, String code) {
    logD("SNOMEDMOCK testIfConceptIsInListInner: code=" + code);

    /* This is a mock and we will do a basic check (is the code Numeric!
    return positive if numeric else false */


    CodeValidationResult retVal = null;

      if (isNumeric(code)) {
          ConceptDefinitionComponent concept = new ConceptDefinitionComponent();
          concept.setCode(code);
          retVal = new CodeValidationResult(concept);
      }
    /* Ignore the list for now KGM Dec 2017 TODO
    for (ConceptDefinitionComponent next : conceptList) {
      // KGM
      logD("SNOMEDMOCK testIfConceptIsInListInner NextCode = "+next.getCode());
      String nextCandidate = next.getCode();



    }
    */

    return retVal;
  }

  @Override
  public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay) {
    CodeSystem cs = fetchCodeSystem(theContext, theCodeSystem);
    logD("SNOMEDMOCK validateCode system = "+ theCodeSystem);

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

    return new CodeValidationResult(IssueSeverity.WARNING, "SNOMEDMOCK Unknown code: " + theCodeSystem + " / " + theCode);
  }

}
