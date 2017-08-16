package uk.gov.hscic.patient.encounters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.patient.EncounterData;

@Service
public class EncounterSearch {

    @Autowired
    private EncounterRepository encounterRepository;

    public List<EncounterData> findEncounterData(final String patientId, Date fromDate, Date toDate, int limit) {
        Pageable pageable = new PageRequest(0, limit);

        if (fromDate != null && toDate != null) {
            return sortItems(encounterRepository.findByNhsNumberAndSectionDateAfterAndSectionDateBeforeOrderBySectionDateDesc(patientId, fromDate, toDate, pageable));
        } else if (fromDate != null) {
            return sortItems(encounterRepository.findByNhsNumberAndSectionDateAfterOrderBySectionDateDesc(patientId, fromDate, pageable));
        } else if (toDate != null) {
            return sortItems(encounterRepository.findByNhsNumberAndSectionDateBeforeOrderBySectionDateDesc(patientId, toDate, pageable));
        }

        return sortItems(encounterRepository.findByNhsNumberOrderBySectionDateDesc(patientId, pageable));
    }

    private List<EncounterData> sortItems(List<EncounterEntity> encounterEntities) {
        List<EncounterData> encounterItemList = new ArrayList<>();

        for (EncounterEntity encounterEntity : encounterEntities) {
            EncounterData encounterData = new EncounterData();
            encounterData.setEncounterDate(encounterEntity.getEncounterDate());
            encounterData.setTitle(encounterEntity.getTitle());
            encounterData.setDetails(encounterEntity.getDetails());

            encounterItemList.add(encounterData);
        }

        return encounterItemList;
    }
}
