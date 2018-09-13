package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.allergy.*;
import uk.nhs.careconnect.ri.database.entity.allergy.*;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class AllergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer implements Transformer<AllergyIntoleranceEntity, AllergyIntolerance> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AllergyIntolerance.class);


    @Override
    public AllergyIntolerance transform(final AllergyIntoleranceEntity allergyEntity) {
        final AllergyIntolerance allergy = new AllergyIntolerance();

        try {
            Meta meta = new Meta().addProfile(CareConnectProfile.AllergyIntolerance_1);

            if (allergyEntity.getUpdated() != null) {
                meta.setLastUpdated(allergyEntity.getUpdated());
            } else {
                if (allergyEntity.getCreated() != null) {
                    meta.setLastUpdated(allergyEntity.getCreated());
                }
            }
            allergy.setMeta(meta);

            allergy.setId(allergyEntity.getId().toString());

            if (allergyEntity.getAssociatedEncounter() != null) {
                allergy.addExtension()
                        .setUrl(CareConnectExtension.UrlAssociatedEncounter)
                        .setValue(new Reference("Encounter/" + allergyEntity.getAssociatedEncounter().getId()));
            }

            if (allergyEntity.getPatient() != null) {
                allergy
                        .setPatient(new Reference("Patient/" + allergyEntity.getPatient().getId())
                                .setDisplay(allergyEntity.getPatient().getNames().get(0).getDisplayName()));
            }
            if (allergyEntity.getAssertedDateTime() != null) {
                allergy.setAssertedDate(allergyEntity.getAssertedDateTime());
            }
            if (allergyEntity.getAsserterPatient() != null) {
                allergy.setAsserter(new Reference("Patient/" + allergyEntity.getAsserterPatient().getId()))
                        .getAsserter().setDisplay(allergyEntity.getAsserterPatient().getNames().get(0).getDisplayName());
            } else if (allergyEntity.getAsserterPractitioner() != null) {
                allergy.setAsserter(new Reference("Practitioner/" + allergyEntity.getAsserterPractitioner().getId()))
                        .getAsserter().setDisplay(allergyEntity.getAsserterPractitioner().getNames().get(0).getDisplayName());
            }
            if (allergyEntity.getClinicalStatus() != null) {
                // FHIR condition ait-2
                if (allergyEntity.getVerificationStatus() == null || !allergyEntity.getVerificationStatus().equals(AllergyIntolerance.AllergyIntoleranceVerificationStatus.ENTEREDINERROR))
                    allergy.setClinicalStatus(allergyEntity.getClinicalStatus());
            }
            if (allergyEntity.getCode() != null) {
                allergy.getCode().addCoding()
                        .setDisplay(allergyEntity.getCode().getDisplay())
                        .setSystem(allergyEntity.getCode().getSystem())
                        .setCode(allergyEntity.getCode().getCode());
            }
            if (allergyEntity.getCriticality() != null) {
                allergy.setCriticality(allergyEntity.getCriticality());
            }
            if (allergyEntity.getLastOccurenceDateTime() != null) {
                allergy.setLastOccurrence(allergyEntity.getLastOccurenceDateTime());
            }
            if (allergyEntity.getNote() != null) {
                // allergy.setNote(allergyEntity.getNote());
            }
            if (allergyEntity.getOnsetDateTime() != null) {
                allergy.setOnset(new DateTimeType().setValue(allergyEntity.getOnsetDateTime()));
            }
            if (allergyEntity.getType() != null) {
                allergy.setType(allergyEntity.getType());
            }
            if (allergyEntity.getVerificationStatus() != null) {
                allergy.setVerificationStatus(allergyEntity.getVerificationStatus());
            }

            for (AllergyIntoleranceReaction reactionEntity : allergyEntity.getReactions()) {
                AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = allergy.addReaction();
                if (reactionEntity.getDescription() != null) {
                    reaction.setDescription(reactionEntity.getDescription());
                }
                if (reactionEntity.getExposureRoute() != null) {
                    reaction.getExposureRoute().addCoding()
                            .setCode(reactionEntity.getExposureRoute().getCode())
                            .setSystem(reactionEntity.getExposureRoute().getSystem())
                            .setDisplay(reactionEntity.getExposureRoute().getDisplay());
                }
                if (reactionEntity.getNote() != null) {
                    //reaction.getNote()
                }
                if (reactionEntity.getOnsetDateTime() != null) {
                    reaction.setOnset(reactionEntity.getOnsetDateTime());
                }
            /* Not in CareConnect
            if (reactionEntity.getSeverity() != null) {
                reaction.setSeverity(reactionEntity.getSeverity());
            }
            */
                if (reactionEntity.getSubstance() != null) {
                    reaction.getSubstance().addCoding()
                            .setCode(reactionEntity.getSubstance().getCode())
                            .setSystem(reactionEntity.getSubstance().getSystem())
                            .setDisplay(reactionEntity.getSubstance().getDisplay());
                }
                for (AllergyIntoleranceManifestation manifestation : reactionEntity.getManifestations()) {
                    if (manifestation.getManifestation() != null) {
                        reaction.addManifestation().addCoding()
                                .setDisplay(manifestation.getManifestation().getDisplay())
                                .setSystem(manifestation.getManifestation().getSystem())
                                .setCode(manifestation.getManifestation().getCode());
                    }
                }

            }
            for (AllergyIntoleranceCategory categoryEntity : allergyEntity.getCategories()) {
                allergy.addCategory(categoryEntity.getCategory());
            }

            for (AllergyIntoleranceIdentifier identifier : allergyEntity.getIdentifiers()) {
                allergy.addIdentifier()
                        .setSystem(identifier.getSystem().getUri())
                        .setValue(identifier.getValue());
            }

        }
        catch (Exception ex) {
                log.error(ex.getMessage());
            }

        return allergy;

    }
}
