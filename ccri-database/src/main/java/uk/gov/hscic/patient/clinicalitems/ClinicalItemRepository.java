package uk.gov.hscic.patient.clinicalitems;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalItemRepository extends JpaRepository<ClinicalItemEntity, Long> {
    List<ClinicalItemEntity> findBynhsNumberOrderBySectionDateDesc(String patientId);
    List<ClinicalItemEntity> findBynhsNumberAndSectionDateAfterOrderBySectionDateDesc(String patientId, Date startDate);
    List<ClinicalItemEntity> findBynhsNumberAndSectionDateBeforeOrderBySectionDateDesc(String patientId, Date endDate);
    List<ClinicalItemEntity> findBynhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(String patientId, Date startDate, Date endDate);
}
