package uk.gov.hscic.patient.observations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservationRepository extends JpaRepository<ObservationEntity, Long> {
    List<ObservationEntity> findBynhsNumber(String patientId);
}
