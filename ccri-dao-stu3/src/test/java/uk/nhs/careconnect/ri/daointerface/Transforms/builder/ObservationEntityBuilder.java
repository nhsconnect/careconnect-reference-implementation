package uk.nhs.careconnect.ri.daointerface.Transforms.builder;

import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationPerformer;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ObservationEntityBuilder {

    public static final long DEFAULT_ID = 2121100L;
    private Long id = DEFAULT_ID;

    public ObservationEntity build() {
        ObservationEntity observationEntity = new ObservationEntity();
        observationEntity.setId(id);

        PatientEntity patient = new PatientEntityBuilder().build();
        observationEntity.setPatient(patient);

        observationEntity.setEffectiveDateTime(new Date());
        List<ObservationPerformer> performers = new ArrayList<>();
        ObservationPerformer performer = new ObservationPerformer();
        performer.setPerformerType(ObservationPerformer.performer.Patient);
        performers.add(performer);
        observationEntity.setPerformers(performers);
        return observationEntity;
    }
}
