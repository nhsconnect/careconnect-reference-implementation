package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.composition.CompositionEntity;
import uk.nhs.careconnect.ri.entity.composition.CompositionIdentifier;
import uk.nhs.careconnect.ri.entity.condition.ConditionCategory;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionIdentifier;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class CompositionEntityToFHIRCompositionTransformer implements Transformer<CompositionEntity, Composition> {


    @Override
    public Composition transform(final CompositionEntity compositionEntity) {
        final Composition composition = new Composition();

        Meta meta = new Meta();
                //.addProfile(CareConnectProfile.Condition_1);

        if (compositionEntity.getUpdated() != null) {
            meta.setLastUpdated(compositionEntity.getUpdated());
        }
        else {
            if (compositionEntity.getCreated() != null) {
                meta.setLastUpdated(compositionEntity.getCreated());
            }
        }
        composition.setMeta(meta);

        composition.setId(compositionEntity.getId().toString());

        if (compositionEntity.getPatient() != null) {
            composition
                    .setSubject(new Reference("Patient/"+compositionEntity.getPatient().getId())
                    .setDisplay(compositionEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (compositionEntity.getDate() != null) {
            composition.setDate(compositionEntity.getDate());
        }
        /*
        if (Entity.getAsserterPractitioner() != null) {
            condition.setAsserter(
                    new Reference("Practitioner/"+conditionEntity.getAsserterPractitioner().getId())
                    .setDisplay(conditionEntity.getAsserterPractitioner().getNames().get(0).getDisplayName())
            );
        }
        */
        if (compositionEntity.getStatus() != null) {
            composition.setStatus(compositionEntity.getStatus());
        }
        if (compositionEntity.getType() != null) {
            composition.getType().addCoding()
                    .setCode(compositionEntity.getType().getCode())
                    .setDisplay(compositionEntity.getType().getDisplay())
                    .setSystem(compositionEntity.getType().getSystem());
        }
        if (compositionEntity.getClass_() != null)
        {
            composition.getClass_().addCoding()
                    .setCode(compositionEntity.getClass_().getCode())
                    .setDisplay(compositionEntity.getClass_().getDisplay())
                    .setSystem(compositionEntity.getClass_().getSystem());
        }

        if (compositionEntity.getEncounter() != null) {
            composition.setEncounter(new Reference("Encounter/"+compositionEntity.getEncounter().getId()));
        }
        for (CompositionIdentifier identifier : compositionEntity.getIdentifiers()) {
            composition.getIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        return composition;

    }
}
