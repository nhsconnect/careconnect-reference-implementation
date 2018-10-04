package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentEntity;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentIdentifier;


@Component
public class AppointmentEntityToFHIRAppointmentTransformer implements Transformer<AppointmentEntity, Appointment> {

    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppointmentEntityToFHIRAppointmentTransformer.class);
    

    @Override
    public Appointment transform(final AppointmentEntity appointmentEntity) {
        final Appointment appointment = new Appointment();

        Meta meta = new Meta(); //.addProfile(CareConnectProfile.Location_1);

        if (appointmentEntity.getUpdated() != null) {
            meta.setLastUpdated(appointmentEntity.getUpdated());
        }
        else {
            if (appointmentEntity.getCreated() != null) {
                meta.setLastUpdated(appointmentEntity.getCreated());
            }
        }
        appointment.setMeta(meta);

        System.out.println("Appointment Metadata:" + appointment.getMeta());
        System.out.println("Appointment Id: " + appointmentEntity.getId().toString());

        if(appointmentEntity.getId() != null){
            appointment.setId(appointmentEntity.getId().toString());
        }

        for(AppointmentIdentifier identifier : appointmentEntity.getIdentifiers())
        {
            appointment.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (appointmentEntity.getStatus() != null) {
            appointment.setStatus(appointmentEntity.getStatus());
        }
/*        if (appointmentEntity.getApointmentType() != null) {
            appointment.setAppointmentType(appointment.getAppointmentType());
        }*/

/*        if (appointmentEntity.getReason() != null) {
            appointment.setReason(appointment.getReason());
        }*/

        if (appointmentEntity.getApointmentType() != null) {
            appointment.getAppointmentType()
                    .addCoding()
                    .setDisplay(appointmentEntity.getApointmentType().getDisplay())
                    .setSystem(appointmentEntity.getApointmentType().getSystem())
                    .setCode(appointmentEntity.getApointmentType().getCode());
        }


        if (appointmentEntity.getPriority() != 0) {
            appointment.setPriority(appointment.getPriority());
        }

        if (appointmentEntity.getDescription() != null) {
            appointment.setDescription(appointmentEntity.getDescription());
        }

        if (appointmentEntity.getStart() != null) {
            appointment.setStart(appointmentEntity.getStart());
        }

        if (appointmentEntity.getEnd() != null) {
            appointment.setEnd(appointmentEntity.getEnd());
        }

/*        if (appointmentEntity.getCategory() != null) {
            appointment.getCategory()
                    .addCoding()
                    .setDisplay(appointmentEntity.getCategory().getDisplay())
                    .setSystem(appointmentEntity.getCategory().getSystem())
                    .setCode(appointmentEntity.getCategory().getCode());
        }*/

/*        if (appointmentEntity.getSlot() != null) {
            appointment.getSlot().add(new Reference("Slot/"+ appointmentEntity.getSlot().getId()));
        }*/

        if (appointmentEntity.getCreated() != null) {
            appointment.setCreated(appointmentEntity.getCreated());
        }

        if (appointmentEntity.getComment() != null) {
            appointment.setComment(appointmentEntity.getComment());
        }

/*        if(appointmentEntity.getParticipant() != null){
            appointment.setParticipant(appointmentEntity.getParticipant());
        }*/

        return appointment;

    }
}
