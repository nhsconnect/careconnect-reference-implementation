package uk.gov.hscic.patient.referrals;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRepository extends JpaRepository<ReferralEntity, Long> {
    List<ReferralEntity> findBynhsNumberOrderBySectionDateDesc(Long patientNHSNumber);
    List<ReferralEntity> findBynhsNumberAndSectionDateAfterOrderBySectionDateDesc(Long patientNHSNumber, Date startDate);
    List<ReferralEntity> findBynhsNumberAndSectionDateBeforeOrderBySectionDateDesc(Long patientNHSNumber, Date endDate);
    List<ReferralEntity> findBynhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(Long patientNHSNumber, Date startDate, Date endDate);
}
