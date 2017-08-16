package uk.gov.hscic.medications;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationHtmlRepository extends JpaRepository<PatientMedicationHtmlEntity, Long> {
    List<PatientMedicationHtmlEntity> findBynhsNumber(String patientId);
}
