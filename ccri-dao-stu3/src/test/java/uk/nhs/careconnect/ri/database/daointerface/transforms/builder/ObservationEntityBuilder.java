package uk.nhs.careconnect.ri.database.daointerface.transforms.builder;

import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationPerformer;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ObservationEntityBuilder {

    public static final long DEFAULT_ID = 2121100L;
    private Long id = DEFAULT_ID;

    public ObservationEntity build() {
        ObservationEntity observationEntity = new ObservationEntity();
        observationEntity.setId(id);

        PatientEntity patient = new PatientEntityBuilder().build();
        observationEntity.setPatient(patient);

        observationEntity.setEffectiveDateTime(new Date());
        Set<ObservationPerformer> performers = new HashSet<>();
        ObservationPerformer performer = new ObservationPerformer();
        performer.setPerformerType(ObservationPerformer.performer.Patient);
        performers.add(performer);
        observationEntity.setPerformers(performers);
        return observationEntity;
    }
}
