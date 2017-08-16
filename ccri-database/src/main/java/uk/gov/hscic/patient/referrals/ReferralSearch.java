package uk.gov.hscic.patient.referrals;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReferralSearch {

    @Autowired
    private ReferralRepository referralRepository;

    public List<ReferralEntity> findReferrals(final String patientId, Date fromDate, Date toDate) {
        if (fromDate != null && toDate != null) {
            return referralRepository.findBynhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(Long.valueOf(patientId), fromDate, toDate);
        } else if (fromDate != null) {
            return referralRepository.findBynhsNumberAndSectionDateAfterOrderBySectionDateDesc(Long.valueOf(patientId), fromDate);
        } else if (toDate != null) {
            return referralRepository.findBynhsNumberAndSectionDateBeforeOrderBySectionDateDesc(Long.valueOf(patientId), toDate);
        }

        return referralRepository.findBynhsNumberOrderBySectionDateDesc(Long.valueOf(patientId));
    }
}
