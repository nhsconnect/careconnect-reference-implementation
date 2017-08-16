package uk.gov.hscic.common.config;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hscic.appointment.appointment.AppointmentStore;
import uk.gov.hscic.appointment.slot.SlotStore;
import uk.gov.hscic.model.appointment.SlotDetail;
import uk.gov.hscic.order.OrderStore;

@Service
public class RefreshData {
    private static final Logger LOG = Logger.getLogger(RefreshData.class);

    @Value("${config.path}")
    private String configPath;

    @Value("${datasource.refresh.slots.filename}")
    private String slotsFilename;

    @Autowired
    private OrderStore orderStore;

    @Autowired
    private AppointmentStore appointmentStore;

    @Autowired
    private SlotStore slotStore;

    // Overnight cleardown of test data
    @Scheduled(cron = "${datasource.cleardown.cron}")
    public void scheduledResetOfData() {
        clearTasks();
        resetAppointments();
    }

    private void clearTasks() {
        orderStore.clearOrders();
    }

    private void resetAppointments() {
        slotStore.clearSlots();
        appointmentStore.clearAppointments();

        try {
            List<String> lines = Files.readLines(new File(configPath + slotsFilename), StandardCharsets.UTF_8);

            for (String line : lines) {
                String[] element = line.split(",");
                Date currentDate = new Date();
                Date startDate = DateUtils.addDays(currentDate, Integer.parseInt(element[0]));
                Date endDate = DateUtils.addDays(currentDate, Integer.parseInt(element[0]));
                startDate.setHours(Integer.parseInt(element[1]));
                startDate.setMinutes(Integer.parseInt(element[2]));
                startDate.setSeconds(Integer.parseInt(element[3]));
                endDate.setHours(Integer.parseInt(element[4]));
                endDate.setMinutes(Integer.parseInt(element[5]));
                endDate.setSeconds(Integer.parseInt(element[6]));
                slotStore.saveSlot(createSlot(Long.parseLong(element[7]), element[8], Long.parseLong(element[9]), element[10], startDate, endDate, currentDate));
            }
        } catch (IOException e) {
            LOG.error("Error reading slots file", e);
        }
    }

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
}
