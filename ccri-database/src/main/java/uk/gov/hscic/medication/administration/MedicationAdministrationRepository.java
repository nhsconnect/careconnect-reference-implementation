package uk.gov.hscic.medication.administration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministrationEntity, Long> {
    List<MedicationAdministrationEntity> findByPatientId(Long patient_id);
}

