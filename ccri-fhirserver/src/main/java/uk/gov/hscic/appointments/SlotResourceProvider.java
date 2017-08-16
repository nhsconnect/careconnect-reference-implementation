package uk.gov.hscic.appointments;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Slot;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.dstu2.valueset.SlotStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.SystemURL;
import uk.gov.hscic.appointment.slot.SlotSearch;
import uk.gov.hscic.model.appointment.SlotDetail;

import java.util.*;

@Component
public class SlotResourceProvider  implements IResourceProvider {

    @Autowired
    private SlotSearch slotSearch;

    @Override
    public Class<Slot> getResourceType() {
        return Slot.class;
    }

    @Read()
    public Slot getSlotById(@IdParam IdDt slotId) {
        SlotDetail slotDetail = slotSearch.findSlotByID(slotId.getIdPartAsLong());

        if (slotDetail == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
            operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No slot details found for ID: " + slotId.getIdPart());
            throw new InternalErrorException("No slot details found for ID: " + slotId.getIdPart(), operationalOutcome);
        }

        return slotDetailToSlotResourceConverter(slotDetail);
    }

    public List<Slot> getSlotsForScheduleId(String scheduleId, Date startDateTime, Date endDateTime) {
        ArrayList<Slot> slots = new ArrayList<>();
        List<SlotDetail> slotDetails = slotSearch.findSlotsForScheduleId(Long.valueOf(scheduleId), startDateTime, endDateTime);

        if (slotDetails != null && !slotDetails.isEmpty()) {
            for (SlotDetail slotDetail : slotDetails) {
                slots.add(slotDetailToSlotResourceConverter(slotDetail));
            }
        }

        return slots;
    }

    public Slot slotDetailToSlotResourceConverter(SlotDetail slotDetail){
        Slot slot = new Slot();
        slot.setId(String.valueOf(slotDetail.getId()));

        if (slotDetail.getLastUpdated() == null) {
            slotDetail.setLastUpdated(new Date());
        }

        slot.getMeta().setLastUpdated(slotDetail.getLastUpdated());
        slot.getMeta().setVersionId(String.valueOf(slotDetail.getLastUpdated().getTime()));
        slot.getMeta().addProfile(SystemURL.SD_GPC_SLOT);
        slot.setIdentifier(Collections.singletonList(new IdentifierDt(SystemURL.ID_GPC_SCHEDULE_IDENTIFIER, String.valueOf(slotDetail.getId()))));
        CodingDt coding = new CodingDt().setSystem(SystemURL.HL7_VS_C80_PRACTICE_CODES).setCode(String.valueOf(slotDetail.getTypeCode())).setDisplay(slotDetail.getTypeDisply());
        CodeableConceptDt codableConcept = new CodeableConceptDt().addCoding(coding);
        codableConcept.setText(slotDetail.getTypeDisply());
        slot.setType(codableConcept);
        slot.setSchedule(new ResourceReferenceDt("Schedule/"+slotDetail.getScheduleReference()));
        slot.setStartWithMillisPrecision(slotDetail.getStartDateTime());
        slot.setEndWithMillisPrecision(slotDetail.getEndDateTime());

        switch (slotDetail.getFreeBusyType().toLowerCase(Locale.UK)) {
            case "free":
                slot.setFreeBusyType(SlotStatusEnum.FREE);
                break;
            default:
                slot.setFreeBusyType(SlotStatusEnum.BUSY);
                break;
        }

        return slot;
    }
}
