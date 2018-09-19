package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentEntity;


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

        appointment.setId(appointmentEntity.getId().toString());

/*
        for(AppointmentIdentifier identifier : appointmentEntity.getIdentifiers())
        {
            appointment.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (appointmentEntity.getActive() != null) {
            appointment.setActive(appointmentEntity.getActive());
        }
        if (appointmentEntity.getName() != null) {
            appointment.setName(appointment.getName());
        }
        if (appointmentEntity.getCategory() != null) {
            appointment.getCategory()
                    .addCoding()
                    .setDisplay(appointmentEntity.getCategory().getDisplay())
                    .setSystem(appointmentEntity.getCategory().getSystem())
                    .setCode(appointmentEntity.getCategory().getCode());
        }

        if (appointmentEntity.getProvidedBy() != null) {
            appointment.setProvidedBy(new Reference("Organization/"+appointmentEntity.getProvidedBy().getId()));
        }
        for (AppointmentSpecialty serviceSpecialty : appointmentEntity.getSpecialties()) {
            service.addSpecialty()
                    .addCoding()
                        .setCode(serviceSpecialty.getSpecialty().getCode())
                        .setSystem(serviceSpecialty.getSpecialty().getSystem())
                        .setDisplay(serviceSpecialty.getSpecialty().getDisplay());
        }
        for (AppointmentLocation serviceLocation : appointmentEntity.getLocations()) {
            service.addLocation(new Reference("Location/"+serviceLocation.getLocation().getId()));
        }
        for (AppointmentTelecom serviceTelecom : appointmentEntity.getTelecoms()) {
            service.addTelecom()
                    .setSystem(serviceTelecom.getSystem())
                    .setValue(serviceTelecom.getValue())
                    .setUse(serviceTelecom.getTelecomUse());

        }
        for (AppointmentType serviceType : appointmentEntity.getTypes()) {
            service.addType()
                    .addCoding()
                    .setCode(serviceType.getType_().getCode())
                    .setSystem(serviceType.getType_().getSystem())
                    .setDisplay(serviceType.getType_().getDisplay());
        }
*/


        return appointment;

    }
}
