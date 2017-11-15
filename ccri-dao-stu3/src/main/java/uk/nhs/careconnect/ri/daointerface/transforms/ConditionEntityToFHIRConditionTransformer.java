package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.BaseAddress;
import uk.nhs.careconnect.ri.entity.condition.ConditionCategory;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;

import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class ConditionEntityToFHIRConditionTransformer implements Transformer<ConditionEntity, Condition> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    public ConditionEntityToFHIRConditionTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public Condition transform(final ConditionEntity conditionEntity) {
        final Condition condition = new Condition();

        Meta meta = new Meta().addProfile(CareConnectProfile.Condition_1);

        if (conditionEntity.getUpdated() != null) {
            meta.setLastUpdated(conditionEntity.getUpdated());
        }
        else {
            if (conditionEntity.getCreated() != null) {
                meta.setLastUpdated(conditionEntity.getCreated());
            }
        }
        condition.setMeta(meta);

        condition.setId(conditionEntity.getId().toString());

        if (conditionEntity.getPatient() != null) {
            condition
                    .setSubject(new Reference("Patient/"+conditionEntity.getPatient().getId())
                    .setDisplay(conditionEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (conditionEntity.getAssertedDateTime() != null) {
            condition.setAssertedDate(conditionEntity.getAssertedDateTime());
        }
        if (conditionEntity.getAsserterPractitioner() != null) {
            condition.setAsserter(new Reference("Practitioner/"+conditionEntity.getAsserterPractitioner().getId()));
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
        if (conditionEntity.getContextEncounter() != null) {
            condition.setContext(new Reference("Encounter/"+conditionEntity.getContextEncounter().getId()));
        } else if (conditionEntity.getContextEpisode() != null) {
            condition.setContext(new Reference("EpisodeOfCare/"+conditionEntity.getContextEpisode().getId()));
        }
        if (conditionEntity.getOnsetDateTime() != null) {
            condition.setOnset(new DateTimeType().setValue(conditionEntity.getOnsetDateTime()));
        }
        if (conditionEntity.getVerificationStatus()!=null) {
            condition.setVerificationStatus(conditionEntity.getVerificationStatus());
        }
        if (conditionEntity.getAssertedDateTime() != null) {
            condition.setAssertedDate(conditionEntity.getAssertedDateTime());
        }
        for (ConditionCategory category : conditionEntity.getCategories()) {
            condition.addCategory().addCoding()
                    .setCode(category.getCategory().getCode())
                    .setSystem(category.getCategory().getSystem())
                    .setDisplay(category.getCategory().getDisplay());
        }


        return condition;

    }
}
