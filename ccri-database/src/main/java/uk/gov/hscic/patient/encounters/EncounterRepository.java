package uk.gov.hscic.patient.encounters;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterRepository extends JpaRepository<EncounterEntity, Long> {
    List<EncounterEntity> findByNhsNumberOrderBySectionDateDesc(String patientNHSNumber, Pageable pageable);
    List<EncounterEntity> findByNhsNumberAndSectionDateAfterOrderBySectionDateDesc(String patientNHSNumber, Date startDate, Pageable pageable);
    List<EncounterEntity> findByNhsNumberAndSectionDateBeforeOrderBySectionDateDesc(String patientNHSNumber, Date endDate, Pageable pageable);
    List<EncounterEntity> findByNhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(String patientNHSNumber, Date startDate, Date endDate, Pageable pageable);
}
