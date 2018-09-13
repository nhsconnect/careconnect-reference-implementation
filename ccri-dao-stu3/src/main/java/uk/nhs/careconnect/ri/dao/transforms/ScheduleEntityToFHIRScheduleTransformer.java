package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Meta;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleIdentifier;


@Component
public class ScheduleEntityToFHIRScheduleTransformer implements Transformer<ScheduleEntity, Schedule> {

    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduleEntityToFHIRScheduleTransformer.class);
    

    @Override
    public Schedule transform(final ScheduleEntity scheduleEntity) {

            final Schedule schedule = new Schedule();

            Meta meta = new Meta(); //.addProfile(CareConnectProfile.Location_1);

            if (scheduleEntity.getUpdated() != null) {
                meta.setLastUpdated(scheduleEntity.getUpdated());
            }
            else {
                if (scheduleEntity.getCreated() != null) {
                    meta.setLastUpdated(scheduleEntity.getCreated());
                }
            }
            schedule.setMeta(meta);

            schedule.setId(scheduleEntity.getId().toString());

            for(ScheduleIdentifier identifier : scheduleEntity.getIdentifiers())
            {
                schedule.addIdentifier()
                        .setSystem(identifier.getSystem().getUri())
                        .setValue(identifier.getValue());
            }

            if (scheduleEntity.getActive() != null) {
                schedule.setActive(scheduleEntity.getActive());
            }
        /* if (scheduleEntity.getName() != null) {
            schedule.setName(schedule.getName());
        }
        if (scheduleEntity.getCategory() != null) {
            schedule.getCategory()
                    .addCoding()
                    .setDisplay(scheduleEntity.getCategory().getDisplay())
                    .setSystem(scheduleEntity.getCategory().getSystem())
                    .setCode(scheduleEntity.getCategory().getCode());
        }

        if (scheduleEntity.getProvidedBy() != null) {
            schedule.setProvidedBy(new Reference("Organization/"+scheduleEntity.getProvidedBy().getId()));
        }
        for (ScheduleSpecialty scheduleSpecialty : scheduleEntity.getSpecialties()) {
            schedule.addSpecialty()
                    .addCoding()
                        .setCode(scheduleSpecialty.getSpecialty().getCode())
                        .setSystem(scheduleSpecialty.getSpecialty().getSystem())
                        .setDisplay(scheduleSpecialty.getSpecialty().getDisplay());
        }
        for (ScheduleLocation scheduleLocation : scheduleEntity.getLocations()) {
            schedule.addLocation(new Reference("Location/"+scheduleLocation.getLocation().getId()));
        }
        for (ScheduleTelecom scheduleTelecom : scheduleEntity.getTelecoms()) {
            schedule.addTelecom()
                    .setSystem(scheduleTelecom.getSystem())
                    .setValue(scheduleTelecom.getValue())
                    .setUse(scheduleTelecom.getTelecomUse());

        }
        for (ScheduleType scheduleType : scheduleEntity.getTypes()) {
            schedule.addType()
                    .addCoding()
                    .setCode(scheduleType.getType_().getCode())
                    .setSystem(scheduleType.getType_().getSystem())
                    .setDisplay(scheduleType.getType_().getDisplay());
        } */


            return schedule;


        }
}
