package uk.nhs.careconnect.ri.stu3.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.stu3.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionCategory;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionIdentifier;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionNote;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class ConditionEntityToFHIRConditionTransformer implements Transformer<ConditionEntity, Condition> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionEntityToFHIRConditionTransformer.class);


    @Override
    public Condition transform(final ConditionEntity conditionEntity) {
        final Condition condition = new Condition();
        try {
            Meta meta = new Meta().addProfile(CareConnectProfile.Condition_1);

            if (conditionEntity.getUpdated() != null) {
                meta.setLastUpdated(conditionEntity.getUpdated());
            } else {
                if (conditionEntity.getCreated() != null) {
                    meta.setLastUpdated(conditionEntity.getCreated());
                }
            }
            condition.setMeta(meta);

            condition.setId(conditionEntity.getId().toString());

            if (conditionEntity.getPatient() != null) {
                condition
                        .setSubject(new Reference("Patient/" + conditionEntity.getPatient().getId())
                                .setDisplay(conditionEntity.getPatient().getNames().get(0).getDisplayName()));
            }
            if (conditionEntity.getAssertedDateTime() != null) {
                condition.setAssertedDate(conditionEntity.getAssertedDateTime());
            }
            if (conditionEntity.getAsserterPractitioner() != null) {
                condition.setAsserter(
                        new Reference("Practitioner/" + conditionEntity.getAsserterPractitioner().getId())
                                .setDisplay(conditionEntity.getAsserterPractitioner().getNames().get(0).getDisplayName())
                );
            }
            if (conditionEntity.getClinicalStatus() != null) {
                condition.setClinicalStatus(conditionEntity.getClinicalStatus());
            }
            if (conditionEntity.getCode() != null) {
                condition.getCode().addCoding()
                        .setCode(conditionEntity.getCode().getCode())
                        .setDisplay(conditionEntity.getCode().getDisplay())
                        .setSystem(conditionEntity.getCode().getSystem());
            }
            if (conditionEntity.getCodeText() != null) {
                condition.getCode().setText(conditionEntity.getCodeText());
            }

            if (conditionEntity.getContextEncounter() != null) {
                condition.setContext(new Reference("Encounter/" + conditionEntity.getContextEncounter().getId()));
            } else if (conditionEntity.getContextEpisode() != null) {
                condition.setContext(new Reference("EpisodeOfCare/" + conditionEntity.getContextEpisode().getId()));
            }
            if (conditionEntity.getOnsetDateTime() != null) {
                condition.setOnset(new DateTimeType().setValue(conditionEntity.getOnsetDateTime()));
            }
        /* Removed from CareConnect Profile 14/May/2018
        if (conditionEntity.getVerificationStatus()!=null) {
            condition.setVerificationStatus(conditionEntity.getVerificationStatus());
        }
        */
            if (conditionEntity.getAssertedDateTime() != null) {
                condition.setAssertedDate(conditionEntity.getAssertedDateTime());
            }
            if (conditionEntity.getSeverity() != null) {
                condition.getSeverity().addCoding()
                        .setCode(conditionEntity.getSeverity().getCode())
                        .setDisplay(conditionEntity.getSeverity().getDisplay())
                        .setSystem(conditionEntity.getSeverity().getSystem());
            }
            if (conditionEntity.getSeverityText() != null) {
                condition.getSeverity().setText(conditionEntity.getSeverityText());
            }

            for (ConditionCategory category : conditionEntity.getCategories()) {
                CodeableConcept concept = condition.addCategory();
                if (category.getCategory() != null) {
                    concept.addCoding()
                        .setCode(category.getCategory().getCode())
                        .setSystem(category.getCategory().getSystem())
                        .setDisplay(category.getCategory().getDisplay());
                }
                if (category.getCategoryText() != null) {
                    concept.setText(category.getCategoryText());
                }
            }
            for (ConditionIdentifier identifier : conditionEntity.getIdentifiers()) {
                Identifier ident = condition.addIdentifier();
                ident = daoutils.getIdentifier(identifier, ident);
            }
            for (ConditionNote conditionNote : conditionEntity.getNotes()) {
                Annotation annotation = condition.addNote();

                annotation.setText(conditionNote.getNote());
                if (conditionNote.getDateTime() != null) {
                    annotation.setTime(conditionNote.getDateTime());
                }
                if (conditionNote.getAuthorPractitoner() != null) {
                    annotation.setAuthor(new Reference("Practitioner/" + conditionNote.getAuthorPractitoner().getId())
                            .setDisplay(conditionNote.getAuthorPractitoner().getNames().get(0).getDisplayName()));
                }

            }
        }
         catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return condition;
    }
}
