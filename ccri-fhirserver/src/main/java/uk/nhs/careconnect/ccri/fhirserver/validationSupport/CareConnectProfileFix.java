package uk.nhs.careconnect.ccri.fhirserver.validationSupport;

import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition;

public class CareConnectProfileFix {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectProfileFix.class);


    public static StructureDefinition fixProfile(StructureDefinition profile) {

        for (ElementDefinition element : profile.getSnapshot().getElement()) {
           fixElement(element);
        }

        for (ElementDefinition element : profile.getDifferential().getElement()) {
            fixElement(element);
        }

        return profile;
    }

    public static ElementDefinition fixElement(ElementDefinition element) {
     //   log.info(element.getId());
        if (element.hasBinding() && element.getBinding().hasValueSetReference()) {
            // Remove invalid SNOMED reference
            if (element.getBinding().getValueSetReference().getReference().equals("http://snomed.info/sct")) {
                log.info("Removing invalid SNOMED valueSet reference");
                element.setBinding(null); // getBinding().setValueSet(null);
            } else
            if (removeTooCostlyECL(element.getBinding().getValueSetReference().getReference())) {
                log.info("Removing costly valueSet reference");
                element.setBinding(null); // getBinding().setValueSet(null);
            }
        }
        if (element.hasBinding() && element.getBinding().hasValueSetUriType()) {
            if (element.getBinding().getValueSetUriType().equals("http://snomed.info/sct")) {
                log.info("Removing invalid SNOMED valueSet reference");
                element.setBinding(null); // getBinding().setValueSet(null);
            } else
            if (removeTooCostlyECL(element.getBinding().getValueSetUriType().getValue())) {
                log.info("Removing costly valueSet reference");
                element.setBinding(null); // getBinding().setValueSet(null);
            }
        }

        if (element.hasBinding() && element.getBinding().hasValueSetReference()) {
            // Remove invalid SNOMED reference

        }
        if (element.hasBinding() && element.getBinding().hasValueSetUriType()) {
            if (element.getBinding().getValueSetUriType().equals("http://snomed.info/sct")) {
                log.info("Removing invalid SNOMED valueSet reference");
                element.setBinding(null); // getBinding().setValueSet(null);
            }
        }

        return element;
    }

    private static Boolean removeTooCostlyECL(String uri) {
        if (uri !=null) {
            // Too costly
            if (uri.equals("https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-MedicationCode-1"))
                return true;
            if (uri.equals("https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-ConditionCode-1"))
                return true;
            if (uri.equals("https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-AllergyCode-1"))
                return true;

            // Invalid ecl

            // Allergy code is also invalid

        }

        return false;
    }
}
