package uk.gov.hscic.medication.orders;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationOrderRepository extends JpaRepository<MedicationOrderEntity, Long> {
    List<MedicationOrderEntity> findByPatientId(Long patient_id);
}
