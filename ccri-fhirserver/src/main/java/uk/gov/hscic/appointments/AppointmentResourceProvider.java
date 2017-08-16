package uk.gov.hscic.appointments;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Appointment;
import ca.uhn.fhir.model.dstu2.resource.Appointment.Participant;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.valueset.AppointmentStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.dstu2.valueset.ParticipationStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.SystemURL;
import uk.gov.hscic.appointment.appointment.AppointmentSearch;
import uk.gov.hscic.appointment.appointment.AppointmentStore;
import uk.gov.hscic.appointment.slot.SlotSearch;
import uk.gov.hscic.appointment.slot.SlotStore;
import uk.gov.hscic.location.LocationSearch;
import uk.gov.hscic.model.appointment.AppointmentDetail;
import uk.gov.hscic.model.appointment.SlotDetail;
import uk.gov.hscic.model.location.LocationDetails;
import uk.gov.hscic.model.patient.PatientDetails;
import uk.gov.hscic.model.practitioner.PractitionerDetails;
import uk.gov.hscic.patient.details.PatientSearch;
import uk.gov.hscic.practitioner.PractitionerSearch;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AppointmentResourceProvider implements IResourceProvider {

    @Autowired
    private AppointmentSearch appointmentSearch;

    @Autowired
    private AppointmentStore appointmentStore;

    @Autowired
    private SlotSearch slotSearch;

    @Autowired
    private SlotStore slotStore;

    @Autowired
    private PatientSearch patientSearch;

    @Autowired
    private PractitionerSearch practitionerSearch;

    @Autowired
    private LocationSearch locationSearch;

    @Override
    public Class<Appointment> getResourceType() {
        return Appointment.class;
    }

    @Read()
    public Appointment getAppointmentById(@IdParam IdDt appointmentId) {
        AppointmentDetail appointmentDetail = appointmentSearch.findAppointmentByID(appointmentId.getIdPartAsLong());

        if (appointmentDetail == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
            operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No appointment details found for ID: " + appointmentId.getIdPart());
            throw new InternalErrorException("No appointment details found for ID: " + appointmentId.getIdPart(), operationalOutcome);
        }

        return appointmentDetailToAppointmentResourceConverter(appointmentDetail);
    }

    @Search
    public List<Appointment> getAppointmentsForPatientIdAndDates(@RequiredParam(name = "patient") IdDt patientLocalId, @OptionalParam(name = "start") DateRangeParam startDate) {
        Date startLowerDate = null;
        Date startUppderDate = null;

        if (startDate != null) {
            if (startDate.getLowerBound() != null) {
                if (startDate.getLowerBound().getPrefix() == ParamPrefixEnum.GREATERTHAN) {
                    startLowerDate = startDate.getLowerBound().getValue();
                } else {
                    if (startDate.getLowerBound().getPrecision() == TemporalPrecisionEnum.DAY) {
                        startLowerDate = startDate.getLowerBound().getValue(); // Remove a day to make time inclusive of parameter date
                    } else {
                        startLowerDate = new Date(startDate.getLowerBound().getValue().getTime() - 1000); // Remove a second to make time inclusive of parameter date
                    }
                }
            }

            if (startDate.getUpperBound() != null) {
                if (startDate.getUpperBound().getPrefix() == ParamPrefixEnum.LESSTHAN) {
                    startUppderDate = startDate.getUpperBound().getValue();
                } else {
                    if (startDate.getUpperBound().getPrecision() == TemporalPrecisionEnum.DAY) {
                        startUppderDate = new Date(startDate.getUpperBound().getValue().getTime() + 86400000); // Add a day to make time inclusive of parameter date
                    } else {
                        startUppderDate = new Date(startDate.getUpperBound().getValue().getTime() + 1000); // Add a second to make time inclusive of parameter date
                    }
                }
            }
        }

        return appointmentSearch.searchAppointments(patientLocalId.getIdPartAsLong(), startLowerDate, startUppderDate)
                .stream()
                .map(this::appointmentDetailToAppointmentResourceConverter)
                .collect(Collectors.toList());
    }

    @Create
    public MethodOutcome createAppointment(@ResourceParam Appointment appointment) {
        if (appointment.getStatus().isEmpty()) {
            throw new UnprocessableEntityException("No status supplied");
        }

        if (appointment.getReason() == null) {
            throw new UnprocessableEntityException("No reason supplied");
        }

        if (appointment.getStart() == null || appointment.getEnd() == null) {
            throw new UnprocessableEntityException("Both start and end date are required");
        }

        if (appointment.getParticipant().isEmpty()) {
            throw new UnprocessableEntityException("Atleast one participant is required");
        }

        for (Participant participant : appointment.getParticipant()) {
            String resourcePart = participant.getActor().getReference().getResourceType();
            String idPart = participant.getActor().getReference().getIdPart();

            switch (resourcePart) {
                case "Patient":
                    PatientDetails patient = patientSearch.findPatientByInternalID(idPart);
                    if (patient == null) {
                        throw new UnprocessableEntityException("Patient resource reference is not a valid resource");
                    }
                    break;
                case "Practitioner":
                    PractitionerDetails practitioner = practitionerSearch.findPractitionerDetails(idPart);
                    if (practitioner == null) {
                        throw new UnprocessableEntityException("Practitioner resource reference is not a valid resource");
                    }
                    break;
                case "Location":
                    LocationDetails location = locationSearch.findLocationById(idPart);
                    if (location == null) {
                        throw new UnprocessableEntityException("Location resource reference is not a valid resource");
                    }
                    break;
            }
        }

        // Store New Appointment
        AppointmentDetail appointmentDetail = appointmentResourceConverterToAppointmentDetail(appointment);
        List<SlotDetail> slots = new ArrayList<>();

        for (Long slotId : appointmentDetail.getSlotIds()) {
            SlotDetail slotDetail = slotSearch.findSlotByID(slotId);

            if (slotDetail == null) {
                throw new UnprocessableEntityException("Slot resource reference is not a valid resource");
            }

            slots.add(slotDetail);
        }

        appointmentDetail = appointmentStore.saveAppointment(appointmentDetail, slots);

        for (SlotDetail slot : slots) {
            slot.setAppointmentId(appointmentDetail.getId());
            slot.setFreeBusyType("BUSY");
            slot.setLastUpdated(new Date());
            slotStore.saveSlot(slot);
        }

        // Build response containing the new resource id
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setId(new IdDt("Appointment", appointmentDetail.getId()));
        methodOutcome.setResource(appointmentDetailToAppointmentResourceConverter(appointmentDetail));
        methodOutcome.setCreated(Boolean.TRUE);

        return methodOutcome;
    }

    @Update
    public MethodOutcome updateAppointment(@IdParam IdDt appointmentId, @ResourceParam Appointment appointment) {
        MethodOutcome methodOutcome = new MethodOutcome();
        OperationOutcome operationalOutcome = new OperationOutcome();

        AppointmentDetail appointmentDetail = appointmentResourceConverterToAppointmentDetail(appointment);

        // URL ID and Resource ID must be the same
        if (!Objects.equals(appointmentId.getIdPartAsLong(), appointmentDetail.getId())) {
            operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("Id in URL (" + appointmentId.getIdPart() + ") should match Id in Resource (" + appointmentDetail.getId() + ")");
            methodOutcome.setOperationOutcome(operationalOutcome);
            return methodOutcome;
        }

        // Make sure there is an existing appointment to be amended
        AppointmentDetail oldAppointmentDetail = appointmentSearch.findAppointmentByID(appointmentId.getIdPartAsLong());
        if (oldAppointmentDetail == null) {
            operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No appointment details found for ID: " + appointmentId.getIdPart());
            methodOutcome.setOperationOutcome(operationalOutcome);
            return methodOutcome;
        }

        String oldAppointmentVersionId = String.valueOf(oldAppointmentDetail.getLastUpdated().getTime());
        String newAppointmentVersionId = appointmentId.getVersionIdPart();
        if (newAppointmentVersionId != null && !newAppointmentVersionId.equalsIgnoreCase(oldAppointmentVersionId)) {
            throw new ResourceVersionConflictException("The specified version (" + newAppointmentVersionId + ") did not match the current resource version (" + oldAppointmentVersionId + ")");
        }

        //Determin if it is a cancel or an amend
        if (appointmentDetail.getCancellationReason() != null) {
            if (appointmentDetail.getCancellationReason().isEmpty()) {
                operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("The cancellation reason can not be blank");
                methodOutcome.setOperationOutcome(operationalOutcome);
                return methodOutcome;
            }

            // This is a Cancellation - so copy across fields which can be altered
            oldAppointmentDetail.setCancellationReason(appointmentDetail.getCancellationReason());
            String oldStatus = oldAppointmentDetail.getStatus();
            appointmentDetail = oldAppointmentDetail;
            appointmentDetail.setStatus("cancelled");

            if (!"cancelled".equalsIgnoreCase(oldStatus)) {
                for (Long slotId : appointmentDetail.getSlotIds()) {
                    SlotDetail slotDetail = slotSearch.findSlotByID(slotId);
                    slotDetail.setAppointmentId(null);
                    slotDetail.setFreeBusyType("FREE");
                    slotDetail.setLastUpdated(new Date());
                    slotStore.saveSlot(slotDetail);
                }
            }
        } else {
            // This is an Amend
            oldAppointmentDetail.setComment(appointmentDetail.getComment());
            oldAppointmentDetail.setReasonCode(appointmentDetail.getReasonCode());
            oldAppointmentDetail.setReasonDisplay(appointmentDetail.getReasonDisplay());
            oldAppointmentDetail.setTypeCode(appointmentDetail.getTypeCode());
            oldAppointmentDetail.setTypeDisplay(appointmentDetail.getTypeDisplay());
            appointmentDetail = oldAppointmentDetail;
        }

        List<SlotDetail> slots = new ArrayList<>();
        for (Long slotId : appointmentDetail.getSlotIds()) {
            SlotDetail slotDetail = slotSearch.findSlotByID(slotId);

            if (slotDetail == null) {
                throw new UnprocessableEntityException("Slot resource reference is not a valid resource");
            }

            slots.add(slotDetail);
        }

        appointmentDetail.setLastUpdated(new Date()); // Update version and lastUpdated timestamp
        appointmentDetail = appointmentStore.saveAppointment(appointmentDetail, slots);

        methodOutcome.setId(new IdDt("Appointment", appointmentDetail.getId()));
        methodOutcome.setResource(appointmentDetailToAppointmentResourceConverter(appointmentDetail));

        return methodOutcome;
    }

    public Appointment appointmentDetailToAppointmentResourceConverter(AppointmentDetail appointmentDetail) {
        Appointment appointment = new Appointment();
        appointment.setId(String.valueOf(appointmentDetail.getId()));
        appointment.getMeta().setLastUpdated(appointmentDetail.getLastUpdated());
        appointment.getMeta().setVersionId(String.valueOf(appointmentDetail.getLastUpdated().getTime()));
        appointment.getMeta().addProfile(SystemURL.SD_GPC_APPOINTMENT);
        appointment.addUndeclaredExtension(false, SystemURL.SD_EXTENSION_GPC_APPOINTMENT_CANCELLATION_REASON, new StringDt(appointmentDetail.getCancellationReason()));
        appointment.setIdentifier(Collections.singletonList(new IdentifierDt(SystemURL.ID_GPC_APPOINTMENT_IDENTIFIER, String.valueOf(appointmentDetail.getId()))));

        switch (appointmentDetail.getStatus().toLowerCase(Locale.UK)) {
            case "pending":
                appointment.setStatus(AppointmentStatusEnum.PENDING);
                break;
            case "booked":
                appointment.setStatus(AppointmentStatusEnum.BOOKED);
                break;
            case "arrived":
                appointment.setStatus(AppointmentStatusEnum.ARRIVED);
                break;
            case "fulfilled":
                appointment.setStatus(AppointmentStatusEnum.FULFILLED);
                break;
            case "cancelled":
                appointment.setStatus(AppointmentStatusEnum.CANCELLED);
                break;
            case "noshow":
                appointment.setStatus(AppointmentStatusEnum.NO_SHOW);
                break;
            default:
                appointment.setStatus(AppointmentStatusEnum.PENDING);
                break;
        }

        CodingDt coding = new CodingDt().setSystem(SystemURL.HL7_VS_C80_PRACTICE_CODES).setCode(String.valueOf(appointmentDetail.getTypeCode())).setDisplay(appointmentDetail.getTypeDisplay());
        CodeableConceptDt codableConcept = new CodeableConceptDt().addCoding(coding);
        codableConcept.setText(appointmentDetail.getTypeDisplay());
        appointment.setType(codableConcept);

        CodingDt codingReason = new CodingDt().setSystem(SystemURL.SNOMED).setCode(String.valueOf(appointmentDetail.getReasonCode())).setDisplay(appointmentDetail.getReasonDisplay());
        CodeableConceptDt codableConceptReason = new CodeableConceptDt().addCoding(codingReason);
        codableConceptReason.setText(appointmentDetail.getReasonDisplay());
        appointment.setReason(codableConceptReason);

        appointment.setStartWithMillisPrecision(appointmentDetail.getStartDateTime());
        appointment.setEndWithMillisPrecision(appointmentDetail.getEndDateTime());

        List<ResourceReferenceDt> slotResources = new ArrayList<>();

        for (Long slotId : appointmentDetail.getSlotIds()) {
            slotResources.add(new ResourceReferenceDt("Slot/" + slotId));
        }

        appointment.setSlot(slotResources);

        appointment.setComment(appointmentDetail.getComment());

        Participant patientParticipant = appointment.addParticipant();
        patientParticipant.setActor(new ResourceReferenceDt("Patient/" + appointmentDetail.getPatientId()));
        patientParticipant.setStatus(ParticipationStatusEnum.ACCEPTED);

        Participant practitionerParticipant = appointment.addParticipant();
        practitionerParticipant.setActor(new ResourceReferenceDt("Practitioner/" + appointmentDetail.getPractitionerId()));
        practitionerParticipant.setStatus(ParticipationStatusEnum.ACCEPTED);

        Participant locationParticipant = appointment.addParticipant();
        locationParticipant.setActor(new ResourceReferenceDt("Location/" + appointmentDetail.getLocationId()));
        locationParticipant.setStatus(ParticipationStatusEnum.ACCEPTED);

        return appointment;
    }

    public AppointmentDetail appointmentResourceConverterToAppointmentDetail(Appointment appointment) {
        AppointmentDetail appointmentDetail = new AppointmentDetail();
        appointmentDetail.setId(appointment.getId().getIdPartAsLong());

        if (appointment.getMeta().getLastUpdated() == null) {
            appointmentDetail.setLastUpdated(new Date());
        } else {
            appointmentDetail.setLastUpdated(appointment.getMeta().getLastUpdated());
        }

        List<ExtensionDt> extension = appointment.getUndeclaredExtensionsByUrl(SystemURL.SD_EXTENSION_GPC_APPOINTMENT_CANCELLATION_REASON);

        if (extension != null && !extension.isEmpty()) {
            String cancellationReason = extension.get(0).getValue().toString();
            appointmentDetail.setCancellationReason(cancellationReason);
        }

        appointmentDetail.setStatus(appointment.getStatus().toLowerCase(Locale.UK));
        //appointmentDetail.setTypeCode(Long.valueOf(appointment.getType().getCodingFirstRep().getCode()));
        appointmentDetail.setTypeDisplay(appointment.getType().getCodingFirstRep().getDisplay());
        if(appointment.getReason().getCodingFirstRep().getCode() == null){
            appointmentDetail.setReasonCode("1");
        } else {
        appointmentDetail.setReasonCode(appointment.getReason().getCodingFirstRep().getCode());
        }
        appointmentDetail.setReasonDisplay(appointment.getReason().getCodingFirstRep().getDisplay());
        appointmentDetail.setStartDateTime(appointment.getStart());
        appointmentDetail.setEndDateTime(appointment.getEnd());

        List<Long> slotIds = new ArrayList<>();

        for (ResourceReferenceDt slotReference : appointment.getSlot()) {
            slotIds.add(slotReference.getReference().getIdPartAsLong());
        }

        appointmentDetail.setSlotIds(slotIds);

        appointmentDetail.setComment(appointment.getComment());

        for (Appointment.Participant participant : appointment.getParticipant()) {
            if (participant.getActor() != null) {
                String participantReference = participant.getActor().getReference().getValue();
                Long actorId = Long.valueOf(participantReference.substring(participantReference.lastIndexOf("/") + 1));
                if (participantReference.contains("Patient/")) {
                    appointmentDetail.setPatientId(actorId);
                } else if (participantReference.contains("Practitioner/")) {
                    appointmentDetail.setPractitionerId(actorId);
                } else if (participantReference.contains("Location/")) {
                    appointmentDetail.setLocationId(actorId);
                }
            }
        }

        return appointmentDetail;
    }
}
