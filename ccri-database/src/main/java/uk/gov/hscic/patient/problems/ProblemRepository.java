package uk.gov.hscic.patient.problems;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {
    List<ProblemEntity> findBynhsNumber(String patientId);
}
