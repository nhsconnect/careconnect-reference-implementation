package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Slot;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.slot.*;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;



@Component
public class SlotEntityToFHIRSlotTransformer implements Transformer<SlotEntity, Slot> {

    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlotEntityToFHIRSlotTransformer.class);
    

    @Override
    public Slot transform(final SlotEntity slotEntity) {
        final Slot slot = new Slot();

        Meta meta = new Meta(); //.addProfile(CareConnectProfile.Location_1);

        if (slotEntity.getUpdated() != null) {
            meta.setLastUpdated(slotEntity.getUpdated());
        }
        else {
            if (slotEntity.getCreated() != null) {
                meta.setLastUpdated(slotEntity.getCreated());
            }
        }
        slot.setMeta(meta);

        slot.setId(slotEntity.getId().toString());

        for(SlotIdentifier identifier : slotEntity.getIdentifiers())
        {
            slot.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

/*        if (slotEntity.getActive() != null) {
            slot.setActive(slotEntity.getActive());
        }*/
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


        return slot;

    }
}
