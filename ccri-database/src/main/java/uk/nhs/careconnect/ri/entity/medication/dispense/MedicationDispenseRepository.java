package uk.nhs.careconnect.ri.entity.medication.dispense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationDispenseRepository extends JpaRepository<MedicationDispenseEntity, Long> {
    List<MedicationDispenseEntity> findByPatientId(Long patient_id);
}
