package uk.gov.hscic.appointment.appointment;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.appointment.slot.SlotEntity;
import uk.gov.hscic.model.appointment.AppointmentDetail;

public class AppointmentEntityToAppointmentDetailTransformer implements Transformer<AppointmentEntity, AppointmentDetail> {

    @Override
    public AppointmentDetail transform(AppointmentEntity item) {
        AppointmentDetail appointmentDetail = new AppointmentDetail();
        appointmentDetail.setId(item.getId());
        appointmentDetail.setCancellationReason(item.getCancellationReason());
        appointmentDetail.setStatus(item.getStatus());
        appointmentDetail.setTypeCode(item.getTypeCode());
        appointmentDetail.setTypeDisplay(item.getTypeDisplay());
        appointmentDetail.setReasonCode(item.getReasonCode());
        appointmentDetail.setReasonDisplay(item.getReasonDisplay());
        appointmentDetail.setStartDateTime(item.getStartDateTime());
        appointmentDetail.setEndDateTime(item.getEndDateTime());

        List<Long> slotIds = new ArrayList<>();

        for (SlotEntity slot : item.getSlots()) {
            slotIds.add(slot.getId());
        }

        appointmentDetail.setSlotIds(slotIds);

        appointmentDetail.setComment(item.getComment());
        appointmentDetail.setPatientId(item.getPatientId());
        appointmentDetail.setPractitionerId(item.getPractitionerId());
        appointmentDetail.setLocationId(item.getLocationId());
        appointmentDetail.setLastUpdated(item.getLastUpdated());
        return appointmentDetail;
    }
}
