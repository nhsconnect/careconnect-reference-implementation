package uk.gov.hscic.patient.adminitems;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.patient.AdminItemData;

@Service
public class AdminItemSearch {

    @Autowired
    private AdminItemRepository adminItemRepository;

    public List<AdminItemData> findAllAdminItemHTMLTables(final String patientId, Date fromDate, Date toDate) {
        if (fromDate != null && toDate != null) {
            return sortItems(adminItemRepository.findBynhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(patientId, fromDate, toDate));
        } else if (fromDate != null) {
            return sortItems(adminItemRepository.findBynhsNumberAndSectionDateAfterOrderBySectionDateDesc(patientId, fromDate));
        } else if (toDate != null) {
            return sortItems(adminItemRepository.findBynhsNumberAndSectionDateBeforeOrderBySectionDateDesc(patientId, toDate));
        }

        return sortItems(adminItemRepository.findBynhsNumberOrderBySectionDateDesc(patientId));
    }

    private List<AdminItemData> sortItems(List<AdminItemEntity> adminItemEntities) {
        List<AdminItemData> adminItemList = new ArrayList<>();

        for (AdminItemEntity adminItemEntity : adminItemEntities) {
            AdminItemData adminItemData = new AdminItemData();
            adminItemData.setAdminDate(adminItemEntity.getAdminDate());
            adminItemData.setDetails(adminItemEntity.getDetails());
            adminItemData.setEntry(adminItemEntity.getEntry());

            adminItemList.add(adminItemData);
        }

        return adminItemList;
    }
}
