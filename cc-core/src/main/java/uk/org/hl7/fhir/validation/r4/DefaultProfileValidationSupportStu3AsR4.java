package uk.org.hl7.fhir.validation.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r4.terminologies.ValueSetExpander;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DefaultProfileValidationSupportStu3AsR4 implements IValidationSupport {

    private static final String URL_PREFIX_VALUE_SET = "http://hl7.org/fhir/ValueSet/";
    private static final String URL_PREFIX_STRUCTURE_DEFINITION = "http://hl7.org/fhir/StructureDefinition/";
    private static final String URL_PREFIX_STRUCTURE_DEFINITION_BASE = "http://hl7.org/fhir/";

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(DefaultProfileValidationSupportStu3AsR4.class);

    private Map<String, CodeSystem> myCodeSystems;
    private Map<String, StructureDefinition> myStructureDefinitions;
    private Map<String, ValueSet> myValueSets;

    private FhirContext stu3Context = FhirContext.forDstu3();

    private void addConcepts(ConceptSetComponent theInclude, ValueSetExpansionComponent theRetVal, Set<String> theWantCodes, List<ConceptDefinitionComponent> theConcepts) {
        for (ConceptDefinitionComponent next : theConcepts) {
            if (theWantCodes.isEmpty() || theWantCodes.contains(next.getCode())) {
                theRetVal
                        .addContains()
                        .setSystem(theInclude.getSystem())
                        .setCode(next.getCode())
                        .setDisplay(next.getDisplay());
            }
            addConcepts(theInclude, theRetVal, theWantCodes, next.getConcept());
        }
    }

    @Override
    public ValueSetExpander.ValueSetExpansionOutcome expandValueSet(FhirContext r4Context, ConceptSetComponent theInclude) {
        ValueSetExpander.ValueSetExpansionOutcome retVal = new ValueSetExpander.ValueSetExpansionOutcome(new ValueSet());

        Set<String> wantCodes = new HashSet<>();
        for (ConceptReferenceComponent next : theInclude.getConcept()) {
            wantCodes.add(next.getCode());
        }

        CodeSystem system = fetchCodeSystem(r4Context, theInclude.getSystem());
        if (system != null) {
            List<ConceptDefinitionComponent> concepts = system.getConcept();
            addConcepts(theInclude, retVal.getValueset().getExpansion(), wantCodes, concepts);
        }

        for (UriType next : theInclude.getValueSet()) {
            ValueSet vs = myValueSets.get(defaultString(next.getValueAsString()));
            if (vs != null) {
                for (ConceptSetComponent nextInclude : vs.getCompose().getInclude()) {
                    ValueSetExpander.ValueSetExpansionOutcome contents = expandValueSet(r4Context, nextInclude);
                    retVal.getValueset().getExpansion().getContains().addAll(contents.getValueset().getExpansion().getContains());
                }
            }
        }

        return retVal;
    }

    @Override
    public List<IBaseResource> fetchAllConformanceResources(FhirContext r4Context) {
        ArrayList<IBaseResource> retVal = new ArrayList<>();
        retVal.addAll(myCodeSystems.values());
        retVal.addAll(myStructureDefinitions.values());
        retVal.addAll(myValueSets.values());
        return retVal;
    }

    @Override
    public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext r4Context) {
        return new ArrayList<>(provideStructureDefinitionMap(r4Context).values());
    }


    @Override
    public CodeSystem fetchCodeSystem(FhirContext r4Context, String theSystem) {
        return (CodeSystem) fetchCodeSystemOrValueSet(r4Context, theSystem, true);
    }

    private DomainResource fetchCodeSystemOrValueSet(FhirContext r4Context, String theSystem, boolean codeSystem) {
        synchronized (this) {
            Map<String, CodeSystem> codeSystems = myCodeSystems;
            Map<String, ValueSet> valueSets = myValueSets;
            if (codeSystems == null || valueSets == null) {
                codeSystems = new HashMap<>();
                valueSets = new HashMap<>();

                loadCodeSystems(r4Context, codeSystems, valueSets, "/org/hl7/fhir/dstu3/model/valueset/valuesets.xml");
                loadCodeSystems(r4Context, codeSystems, valueSets, "/org/hl7/fhir/dstu3/model/valueset/v2-tables.xml");
                loadCodeSystems(r4Context, codeSystems, valueSets, "/org/hl7/fhir/dstu3/model/valueset/v3-codesystems.xml");

                myCodeSystems = codeSystems;
                myValueSets = valueSets;
            }

            // System can take the form "http://url|version"
            String system = theSystem;
            if (system.contains("|")) {
                String version = system.substring(system.indexOf('|') + 1);
                if (version.matches("^[0-9.]+$")) {
                    system = system.substring(0, system.indexOf('|'));
                }
            }

            if (codeSystem) {
                return codeSystems.get(system);
            } else {
                return valueSets.get(system);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext r4Context, Class<T> theClass, String theUri) {
        Validate.notBlank(theUri, "theUri must not be null or blank");

        if (theClass.equals(StructureDefinition.class)) {
            return (T) fetchStructureDefinition(r4Context, theUri);
        }

        if (theClass.equals(ValueSet.class) || theUri.startsWith(URL_PREFIX_VALUE_SET)) {
            return (T) fetchValueSet(r4Context, theUri);
        }

        return null;
    }

    @Override
    public StructureDefinition fetchStructureDefinition(FhirContext r4Context, String theUrl) {
        String url = theUrl;
        if (url.startsWith(URL_PREFIX_STRUCTURE_DEFINITION)) {
            // no change
        } else if (url.indexOf('/') == -1) {
            url = URL_PREFIX_STRUCTURE_DEFINITION + url;
        } else if (StringUtils.countMatches(url, '/') == 1) {
            url = URL_PREFIX_STRUCTURE_DEFINITION_BASE + url;
        }
        return provideStructureDefinitionMap(r4Context).get(url);
    }

    public ValueSet fetchValueSet(FhirContext r4Context, String theSystem) {
        return (ValueSet) fetchCodeSystemOrValueSet(r4Context, theSystem, false);
    }

    public void flush() {
        myCodeSystems = null;
        myStructureDefinitions = null;
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext r4Context, String theSystem) {
        CodeSystem cs = fetchCodeSystem(r4Context, theSystem);
        return cs != null && cs.getContent() != CodeSystemContentMode.NOTPRESENT;
    }

    private void loadCodeSystems(FhirContext r4Context, Map<String, CodeSystem> theCodeSystems, Map<String, ValueSet> theValueSets, String theClasspath) {
        ourLog.info("Loading CodeSystem/ValueSet from classpath: {}", theClasspath);
        InputStream inputStream = DefaultProfileValidationSupportStu3AsR4.class.getResourceAsStream(theClasspath);
        InputStreamReader reader = null;
        if (inputStream != null) {
            try {
                reader = new InputStreamReader(inputStream, Constants.CHARSET_UTF8);

                org.hl7.fhir.dstu3.model.Bundle bundle = stu3Context.newXmlParser().parseResource(org.hl7.fhir.dstu3.model.Bundle.class, reader);
                for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent next : bundle.getEntry()) {
                    if (next.getResource() instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
                        CodeSystem nextValueSet = (CodeSystem) convertToR4((org.hl7.fhir.dstu3.model.CodeSystem) next.getResource());
                        nextValueSet.getText().setDivAsString("");
                        String system = nextValueSet.getUrl();
                        if (isNotBlank(system)) {
                            theCodeSystems.put(system, nextValueSet);
                        }
                    } else if (next.getResource() instanceof org.hl7.fhir.dstu3.model.ValueSet) {
                        ValueSet nextValueSet = (ValueSet) convertToR4((org.hl7.fhir.dstu3.model.ValueSet) next.getResource());
                        nextValueSet.getText().setDivAsString("");
                        String system = nextValueSet.getUrl();
                        if (isNotBlank(system)) {
                            theValueSets.put(system, nextValueSet);
                        }
                    }
                }
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    inputStream.close();
                } catch (IOException e) {
                    ourLog.warn("Failure closing stream", e);
                }
            }
        } else {
            ourLog.warn("Unable to load resource: {}", theClasspath);
        }
    }

    private void loadStructureDefinitions(FhirContext r4Context, Map<String, StructureDefinition> theCodeSystems, String theClasspath) {
        ourLog.info("Loading structure definitions from classpath: {}", theClasspath);
        InputStream valuesetText = DefaultProfileValidationSupportStu3AsR4.class.getResourceAsStream(theClasspath);
        if (valuesetText != null) {
            InputStreamReader reader = new InputStreamReader(valuesetText, Constants.CHARSET_UTF8);

            org.hl7.fhir.dstu3.model.Bundle bundle = stu3Context.newXmlParser().parseResource(org.hl7.fhir.dstu3.model.Bundle.class, reader);
            for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent next : bundle.getEntry()) {
                if (next.getResource() instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
                    StructureDefinition nextSd = (StructureDefinition) convertToR4((org.hl7.fhir.dstu3.model.StructureDefinition) next.getResource());
                    nextSd.getText().setDivAsString("");
                    String system = nextSd.getUrl();
                    if (isNotBlank(system)) {
                        theCodeSystems.put(system, nextSd);
                    }
                }
            }
        } else {
            ourLog.warn("Unable to load resource: {}", theClasspath);
        }
    }

    private Map<String, StructureDefinition> provideStructureDefinitionMap(FhirContext r4Context) {
        Map<String, StructureDefinition> structureDefinitions = myStructureDefinitions;
        if (structureDefinitions == null) {
            structureDefinitions = new HashMap<>();

            loadStructureDefinitions(r4Context, structureDefinitions, "/org/hl7/fhir/dstu3/model/profile/profiles-resources.xml");
            loadStructureDefinitions(r4Context, structureDefinitions, "/org/hl7/fhir/dstu3/model/profile/profiles-types.xml");
            loadStructureDefinitions(r4Context, structureDefinitions, "/org/hl7/fhir/dstu3/model/profile/profiles-others.xml");
            loadStructureDefinitions(r4Context, structureDefinitions, "/org/hl7/fhir/dstu3/model/extension/extension-definitions.xml");

            myStructureDefinitions = structureDefinitions;
        }
        return structureDefinitions;
    }

    private CodeValidationResult testIfConceptIsInList(String theCode, List<ConceptDefinitionComponent> conceptList, boolean theCaseSensitive) {
        String code = theCode;
        if (theCaseSensitive == false) {
            code = code.toUpperCase();
        }

        return testIfConceptIsInListInner(conceptList, theCaseSensitive, code);
    }

    private CodeValidationResult testIfConceptIsInListInner(List<ConceptDefinitionComponent> conceptList, boolean theCaseSensitive, String code) {
        CodeValidationResult retVal = null;
        for (ConceptDefinitionComponent next : conceptList) {
            String nextCandidate = next.getCode();
            if (theCaseSensitive == false) {
                nextCandidate = nextCandidate.toUpperCase();
            }
            if (nextCandidate.equals(code)) {
                retVal = new CodeValidationResult(next);
                break;
            }

            // recurse
            retVal = testIfConceptIsInList(code, next.getConcept(), theCaseSensitive);
            if (retVal != null) {
                break;
            }
        }

        return retVal;
    }

    @Override
    public CodeValidationResult validateCode(FhirContext r4Context, String theCodeSystem, String theCode, String theDisplay) {
        CodeSystem cs = fetchCodeSystem(r4Context, theCodeSystem);
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

        return new CodeValidationResult(IssueSeverity.WARNING, "Unknown code: " + theCodeSystem + " / " + theCode);
    }

    public IBaseResource convertToR4(IBaseResource stu3resource) {
        if (stu3resource == null) return null;
        VersionConvertor_30_40 convertor = new VersionConvertor_30_40();
        return convertor.convertResource((org.hl7.fhir.dstu3.model.Resource) stu3resource, true);


        // log.info(this.r4ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(convertedResource));

    }

}
