package uk.gov.hscic.appointment.appointment;

import uk.gov.hscic.appointment.slot.SlotDetailToSlotEntityTransformer;
import uk.gov.hscic.appointment.slot.SlotEntity;
import uk.gov.hscic.model.appointment.AppointmentDetail;
import uk.gov.hscic.model.appointment.SlotDetail;

import java.util.ArrayList;
import java.util.List;

public class AppointmentDetailToAppointmentEntityTransformer {

    public AppointmentEntity transform(AppointmentDetail item, List<SlotDetail> slots) {
        AppointmentEntity appointmentEntity = new AppointmentEntity();
        appointmentEntity.setId(item.getId());

        SlotDetailToSlotEntityTransformer slotTransformer = new SlotDetailToSlotEntityTransformer();
        List<SlotEntity> slotsEntities = new ArrayList<>();

        for (SlotDetail slot : slots) {
            slotsEntities.add(slotTransformer.transform(slot));
        }

        appointmentEntity.setSlots(slotsEntities);

        appointmentEntity.setCancellationReason(item.getCancellationReason());
        appointmentEntity.setStatus(item.getStatus());
        appointmentEntity.setTypeCode(item.getTypeCode());
        appointmentEntity.setTypeDisplay(item.getTypeDisplay());
        appointmentEntity.setReasonCode(item.getReasonCode());
        appointmentEntity.setReasonDisplay(item.getReasonDisplay());
        appointmentEntity.setStartDateTime(item.getStartDateTime());
        appointmentEntity.setEndDateTime(item.getEndDateTime());
        appointmentEntity.setComment(item.getComment());
        appointmentEntity.setPatientId(item.getPatientId());
        appointmentEntity.setPractitionerId(item.getPractitionerId());
        appointmentEntity.setLocationId(item.getLocationId());
        appointmentEntity.setLastUpdated(item.getLastUpdated());

        return appointmentEntity;
    }
}
