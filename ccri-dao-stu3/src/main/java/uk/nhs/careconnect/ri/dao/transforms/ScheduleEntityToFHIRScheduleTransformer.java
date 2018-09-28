package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Meta;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleActor;
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

            for(ScheduleActor actor : scheduleEntity.getActors()){
                if(actor.getPractitionerRole() != null){
                    schedule.addActor().setReference("PractitonerRole/"+actor.getPractitionerRole().getId());
                }

                if(actor.getPractitionerEntity() != null){
                    schedule.addActor().setReference("Practitoner/"+actor.getPractitionerEntity().getId());
                }

                if(actor.getHealthcareServiceEntity() != null){
                    schedule.addActor().setReference("HealthcareService/"+actor.getHealthcareServiceEntity().getId());
                }

                if(actor.getLocationEntity() != null){
                    schedule.addActor().setReference("Location/"+actor.getLocationEntity().getId());
                }

            }

            if (scheduleEntity.getComment() != null) {
                schedule.setComment(scheduleEntity.getComment());
            }


            if (scheduleEntity.getCategory() != null) {
                schedule.getServiceCategory()
                        .addCoding()
                        .setDisplay(scheduleEntity.getCategory().getDisplay())
                        .setSystem(scheduleEntity.getCategory().getSystem())
                        .setCode(scheduleEntity.getCategory().getCode());
            }

            return schedule;

    }
}
