package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.observation.ObservationCategory;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationPerformer;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class ObservationEntityToFHIRObservationTransformer implements Transformer<ObservationEntity, Observation> {
    @Override
    public Observation transform(final ObservationEntity observationEntity) {
        Observation observation = new Observation();

        Meta meta = new Meta().addProfile(CareConnectProfile.Observation_1);

        if (observationEntity.getUpdated() != null) {
            meta.setLastUpdated(observationEntity.getUpdated());
        }
        else {
            if (observationEntity.getCreated() != null) {
                meta.setLastUpdated(observationEntity.getCreated());
            }
        }
        observation.setMeta(meta);

        observation.setId(observationEntity.getId().toString());

        observation.setEffective(new DateTimeType(observationEntity.getEffectiveDateTime()));
        observation.setSubject(new Reference("Patient/"+observationEntity.getPatient().getId()));

        if (observationEntity.getCode() != null) {
            observation.getCode().addCoding()
                    .setCode(observationEntity.getCode().getCode())
                    .setSystem(observationEntity.getCode().getSystem())
                    .setDisplay(observationEntity.getCode().getDisplay());
        }

        for (ObservationCategory category : observationEntity.getCategories()) {
            CodeableConcept concept = new CodeableConcept();
            concept.addCoding()
                    .setCode(category.getCategory().getCode())
                    .setSystem(category.getCategory().getSystem())
                    .setDisplay(category.getCategory().getDisplay());

            observation.getCategory().add(concept);
        }
        for (ObservationPerformer performer :observationEntity.getPerformers()) {
            switch (performer.getPerformerType()) {
                case Practitioner:
                    observation.getPerformer().add(new Reference("Practioner/"+performer.getPerformerPractitioner().getId())
                            .setDisplay(performer.getPerformerPractitioner().getNames().get(0).getDisplayName()));
            }
        }
        if (observationEntity.getValueQuantity() != null) {
            Quantity quantity = new Quantity();
            quantity.setValue(observationEntity.getValueQuantity())
                .setCode(observationEntity.getValueUnitOfMeasure().getCode())
                .setSystem(observationEntity.getValueUnitOfMeasure().getSystem())
                .setUnit(observationEntity.getValueUnitOfMeasure().getCode());

            observation.setValue(quantity);
        }
        for (ObservationEntity componentEntity : observationEntity.getComponents()) {
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            if (componentEntity.getCode() != null) {
                component.getCode().addCoding()
                        .setCode(componentEntity.getCode().getCode())
                        .setSystem(componentEntity.getCode().getSystem())
                        .setDisplay(componentEntity.getCode().getDisplay());
            }
            if (componentEntity.getValueQuantity() != null) {
                Quantity quantity = new Quantity();
                quantity.setValue(componentEntity.getValueQuantity())
                        .setCode(componentEntity.getValueUnitOfMeasure().getCode())
                        .setSystem(componentEntity.getValueUnitOfMeasure().getSystem())
                        .setUnit(componentEntity.getValueUnitOfMeasure().getCode());

                component.setValue(quantity);
            }
            observation.getComponent().add(component);
        }

        observation.setStatus(observationEntity.getStatus());
        return observation;
    }
}
