package uk.nhs.careconnect.ri.daointerface.Transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationPerformer;

@Component
public class ObservationEntityToFHIRObservationTransformer implements Transformer<ObservationEntity, Observation> {
    @Override
    public Observation transform(final ObservationEntity observationEntity) {
        Observation observation = new Observation();

        observation.setEffective(new DateTimeType(observationEntity.getEffectiveDateTime()));
        observation.setSubject(new Reference("Patient/"+observationEntity.getPatient().getId()));

        if (observation.getCode() != null) {
            observation.getCode().addCoding()
                    .setCode(observationEntity.getCode().getCode())
                    .setSystem(observationEntity.getCode().getSystem())
                    .setDisplay(observationEntity.getCode().getDisplay());
        }
        for (ObservationPerformer performer :observationEntity.getPerformers()) {
            switch (performer.getPerformerType()) {
                case Practitioner:
                    observation.getPerformer().add(new Reference("Practioner/"+performer.getPerformerPractitioner().getId()).setDisplay(performer.getPerformerPractitioner().getNames().get(0).getFamilyName()));
            }
        }
        observation.setStatus(observationEntity.getStatus());
        return observation;
    }
}
