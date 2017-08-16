package uk.gov.hscic.patient.adminitems;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminItemRepository extends JpaRepository<AdminItemEntity, Long> {
    List<AdminItemEntity> findBynhsNumberOrderBySectionDateDesc(String patientNHSNumber);
    List<AdminItemEntity> findBynhsNumberAndSectionDateAfterOrderBySectionDateDesc(String patientNHSNumber, Date startDate);
    List<AdminItemEntity> findBynhsNumberAndSectionDateBeforeOrderBySectionDateDesc(String patientNHSNumber, Date endDate);
    List<AdminItemEntity> findBynhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(String patientNHSNumber, Date startDate, Date endDate);
}
