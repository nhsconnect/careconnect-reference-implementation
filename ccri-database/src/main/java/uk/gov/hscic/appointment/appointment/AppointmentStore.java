package uk.gov.hscic.appointment.appointment;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.appointment.AppointmentDetail;
import uk.gov.hscic.appointment.appointment.AppointmentEntity;
import uk.gov.hscic.appointment.appointment.AppointmentRepository;
import uk.gov.hscic.appointment.appointment.AppointmentEntityToAppointmentDetailTransformer;
import uk.gov.hscic.model.appointment.SlotDetail;

@Service
public class AppointmentStore {
    private final AppointmentEntityToAppointmentDetailTransformer entityToDetailTransformer = new AppointmentEntityToAppointmentDetailTransformer();
    private final AppointmentDetailToAppointmentEntityTransformer detailToEntityTransformer = new AppointmentDetailToAppointmentEntityTransformer();

    @Autowired
    private AppointmentRepository appointmentRepository;

    public AppointmentDetail saveAppointment(AppointmentDetail appointment, List<SlotDetail> slots){
        AppointmentEntity appointmentEntity = detailToEntityTransformer.transform(appointment, slots);
        appointmentEntity = appointmentRepository.saveAndFlush(appointmentEntity);
        return entityToDetailTransformer.transform(appointmentEntity);
    }

    public void clearAppointments(){
        appointmentRepository.deleteAll();
    }
}
