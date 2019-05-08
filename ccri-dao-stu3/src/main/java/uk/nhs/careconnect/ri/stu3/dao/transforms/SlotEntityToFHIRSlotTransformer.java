package uk.nhs.careconnect.ri.stu3.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Slot;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.stu3.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotIdentifier;

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
            Identifier ident = slot.addIdentifier();
            ident = daoutils.getIdentifier(identifier, ident);
        }

        if (slotEntity.getServiceCategory() != null) {
            slot.getServiceCategory()
                    .addCoding()
                    .setDisplay(slotEntity.getServiceCategory().getDisplay())
                    .setSystem(slotEntity.getServiceCategory().getSystem())
                    .setCode(slotEntity.getServiceCategory().getCode());
        }

        if (slotEntity.getAppointmentType() != null) {
            slot.getAppointmentType()
                    .addCoding()
                    .setDisplay(slotEntity.getAppointmentType().getDisplay())
                    .setSystem(slotEntity.getAppointmentType().getSystem())
                    .setCode(slotEntity.getAppointmentType().getCode());
        }

        if (slotEntity.getSchedule() != null) {
            slot.setSchedule(new Reference("Schedule/"+slotEntity.getSchedule().getId()));
        }

        if (slotEntity.getStatus() != null) {
            slot.setStatus(slotEntity.getStatus());
        }
        if (slotEntity.getStart() != null) {
            slot.setStart(slotEntity.getStart());
        }

        if (slotEntity.getEnd() != null) {
            slot.setEnd(slotEntity.getEnd());
        }

        return slot;

    }
}
