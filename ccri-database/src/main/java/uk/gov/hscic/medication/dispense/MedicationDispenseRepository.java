package uk.gov.hscic.medication.dispense;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationDispenseRepository extends JpaRepository<MedicationDispenseEntity, Long> {
    List<MedicationDispenseEntity> findByPatientId(Long patient_id);
}
