package uk.gov.hscic.patient.allergies;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergyRepository extends JpaRepository<AllergyEntity, Long> {
    List<AllergyEntity> findByNhsNumber(String patientId);
}
