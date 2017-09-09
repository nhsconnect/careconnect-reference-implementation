package uk.nhs.careconnect.ri.common.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RefreshData {
    private static final Logger LOG = Logger.getLogger(RefreshData.class);

    @Value("${config.path}")
    private String configPath;

    @Value("${datasource.refresh.slots.filename}")
    private String slotsFilename;

   // @Autowired
   // private OrderStore orderStore;



    // Overnight cleardown of test data
    @Scheduled(cron = "${datasource.cleardown.cron}")
    public void scheduledResetOfData() {
       // clearTasks();
       // resetAppointments();
    }

/*  private void clearTasks() {
        orderStore.clearOrders();
    }
*/

/*
    private SlotDetail createSlot(Long typeCode, String typeDisplay, long scheduleReference, String freeBusy, Date startDate, Date endDate, Date lastUpdated) {
        SlotDetail slot = new SlotDetail();
        slot.setTypeCode(typeCode);
        slot.setTypeDisply(typeDisplay);
        slot.setScheduleReference(scheduleReference);
        slot.setFreeBusyType(freeBusy);
        slot.setStartDateTime(startDate);
        slot.setEndDateTime(endDate);
        slot.setLastUpdated(lastUpdated);
        return slot;
    }

    */
}
