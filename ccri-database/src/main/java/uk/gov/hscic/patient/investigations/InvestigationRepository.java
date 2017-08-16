package uk.gov.hscic.patient.investigations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationRepository extends JpaRepository<InvestigationEntity, Long> {
    List<InvestigationEntity> findByNhsNumber(String nhsNumber);
}
