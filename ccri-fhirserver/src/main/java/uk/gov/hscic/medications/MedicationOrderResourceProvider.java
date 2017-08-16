package uk.gov.hscic.medications;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationOrderStatusEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.SystemURL;
import uk.gov.hscic.model.medication.MedicationOrderDetails;
import uk.gov.hscic.medication.orders.MedicationOrderSearch;

@Component
public class MedicationOrderResourceProvider implements IResourceProvider {

    @Autowired
    private MedicationOrderSearch medicationOrderSearch;

    @Override
    public Class<MedicationOrder> getResourceType() {
        return MedicationOrder.class;
    }

    @Search
    public List<MedicationOrder> getMedicationOrdersForPatientId(@RequiredParam(name = "patient") String patientId) {
        ArrayList<MedicationOrder> medicationOrders = new ArrayList<>();
        List<MedicationOrderDetails> medicationOrderDetailsList = medicationOrderSearch.findMedicationOrdersForPatient(Long.parseLong(patientId));

        if (medicationOrderDetailsList != null && !medicationOrderDetailsList.isEmpty()) {
            for (MedicationOrderDetails medicationOrderDetails : medicationOrderDetailsList) {
                medicationOrders.add(medicationOrderDetailsToMedicationOrderResourceConverter(medicationOrderDetails));
            }
        }

        return medicationOrders;
    }

    @Read()
    public MedicationOrder getMedicationOrderById(@IdParam IdDt medicationOrderId) {
        MedicationOrderDetails medicationOrderDetails = medicationOrderSearch.findMedicationOrderByID(medicationOrderId.getIdPartAsLong());

        if (medicationOrderDetails == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
            operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No medicationOrder details found for ID: " + medicationOrderId.getIdPart());
            throw new InternalErrorException("No medicationOrder details found for ID: " + medicationOrderId.getIdPart(), operationalOutcome);
        }

        return medicationOrderDetailsToMedicationOrderResourceConverter(medicationOrderDetails);
    }


    public MedicationOrder medicationOrderDetailsToMedicationOrderResourceConverter(MedicationOrderDetails medicationOrderDetails) {
        MedicationOrder medicationOrder = new MedicationOrder();
        medicationOrder.setId(String.valueOf(medicationOrderDetails.getId()));
        medicationOrder.getMeta().setLastUpdated(medicationOrderDetails.getLastUpdated());
        medicationOrder.getMeta().setVersionId(String.valueOf(medicationOrderDetails.getLastUpdated().getTime()));
        medicationOrder.setDateWritten(new DateTimeDt(medicationOrderDetails.getDateWritten()));

        switch (medicationOrderDetails.getOrderStatus().toLowerCase(Locale.UK)) {
            case "active":
                medicationOrder.setStatus(MedicationOrderStatusEnum.ACTIVE);
                break;
            case "completed":
                medicationOrder.setStatus(MedicationOrderStatusEnum.COMPLETED);
                break;
            case "draft":
                medicationOrder.setStatus(MedicationOrderStatusEnum.DRAFT);
                break;
            case "entered_in_error":
                medicationOrder.setStatus(MedicationOrderStatusEnum.ENTERED_IN_ERROR);
                break;
            case "on_hold":
                medicationOrder.setStatus(MedicationOrderStatusEnum.ON_HOLD);
                break;
            case "stopped":
                medicationOrder.setStatus(MedicationOrderStatusEnum.STOPPED);
                break;
        }

        if (medicationOrderDetails.getPatientId() != null) {
            medicationOrder.setPatient(new ResourceReferenceDt("Patient/"+medicationOrderDetails.getPatientId()));
        } else {
            medicationOrder.setPatient(new ResourceReferenceDt());
        }

        medicationOrder.setPrescriber(new ResourceReferenceDt("Practitioner/"+medicationOrderDetails.getAutherId()));
        medicationOrder.setMedication(new ResourceReferenceDt("Medication/"+medicationOrderDetails.getMedicationId()));
        medicationOrder.addDosageInstruction().setText(medicationOrderDetails.getDosageText());

        MedicationOrder.DispenseRequest dispenseRequest = new MedicationOrder.DispenseRequest();
        dispenseRequest.addUndeclaredExtension(new ExtensionDt(false, SystemURL.SD_EXTENSION_MEDICATION_QUANTITY_TEXT, new StringDt(medicationOrderDetails.getDispenseQuantityText())));
        dispenseRequest.addUndeclaredExtension(new ExtensionDt(true, SystemURL.SD_EXTENSION_PERSCRIPTION_REPEAT_REVIEW_DATE, new DateTimeDt(medicationOrderDetails.getDispenseReviewDate())));
        dispenseRequest.setMedication(new ResourceReferenceDt("Medication/"+medicationOrderDetails.getDispenseMedicationId()));
        dispenseRequest.setNumberOfRepeatsAllowed(medicationOrderDetails.getDispenseRepeatsAllowed());
        medicationOrder.setDispenseRequest(dispenseRequest);

        return medicationOrder;
    }
}
