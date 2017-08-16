package uk.gov.hscic.patient.clinicalitems;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.patient.ClinicalItemData;

@Service
public class ClinicalItemSearch {

    @Autowired
    private ClinicalItemRepository clinicalItemRepository;

    public List<ClinicalItemData> findAllClinicalItemHTMLTables(final String patientId, Date fromDate, Date toDate) {
        if (fromDate != null && toDate != null) {
            return sortItems(clinicalItemRepository.findBynhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(patientId, fromDate, toDate));
        } else if (fromDate != null) {
            return sortItems(clinicalItemRepository.findBynhsNumberAndSectionDateAfterOrderBySectionDateDesc(patientId, fromDate));
        } else if (toDate != null) {
            return sortItems(clinicalItemRepository.findBynhsNumberAndSectionDateBeforeOrderBySectionDateDesc(patientId, toDate));
        }

        return sortItems(clinicalItemRepository.findBynhsNumberOrderBySectionDateDesc(patientId));
    }

    private List<ClinicalItemData> sortItems(List<ClinicalItemEntity> items) {
        List<ClinicalItemData> clinicalItemList = new ArrayList<>();

        for (ClinicalItemEntity clinicalItemEntity : items) {
            ClinicalItemData clinicalItemData = new ClinicalItemData();
            clinicalItemData.setDate(clinicalItemEntity.getDate());
            clinicalItemData.setDetails(clinicalItemEntity.getDetails());
            clinicalItemData.setEntry(clinicalItemEntity.getEntry());

            clinicalItemList.add(clinicalItemData);
        }

        return clinicalItemList;
    }
}
