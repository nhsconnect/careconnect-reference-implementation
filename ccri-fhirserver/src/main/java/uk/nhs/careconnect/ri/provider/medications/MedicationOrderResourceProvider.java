package uk.nhs.careconnect.ri.provider.medications;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.medication.orders.MedicationOrderSearch;
import uk.nhs.careconnect.ri.model.medication.MedicationOrderDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public MedicationOrder getMedicationOrderById(@IdParam IdType medicationOrderId) {
        MedicationOrderDetails medicationOrderDetails = medicationOrderSearch.findMedicationOrderByID(medicationOrderId.getIdPartAsLong());

        if (medicationOrderDetails == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
            // TODO operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No medicationOrder details found for ID: " + medicationOrderId.getIdPart());
            throw new InternalErrorException("No medicationOrder details found for ID: " + medicationOrderId.getIdPart(), operationalOutcome);
        }

        return medicationOrderDetailsToMedicationOrderResourceConverter(medicationOrderDetails);
    }


    public MedicationOrder medicationOrderDetailsToMedicationOrderResourceConverter(MedicationOrderDetails medicationOrderDetails) {
        MedicationOrder medicationOrder = new MedicationOrder();
        medicationOrder.setId(String.valueOf(medicationOrderDetails.getId()));
        medicationOrder.getMeta().setLastUpdated(medicationOrderDetails.getLastUpdated());
        medicationOrder.getMeta().setVersionId(String.valueOf(medicationOrderDetails.getLastUpdated().getTime()));
        medicationOrder.setDateWritten(medicationOrderDetails.getDateWritten());

        switch (medicationOrderDetails.getOrderStatus().toLowerCase(Locale.UK)) {
            case "active":
                medicationOrder.setStatus(MedicationOrder.MedicationOrderStatus.ACTIVE);
                break;
            case "completed":
                medicationOrder.setStatus(MedicationOrder.MedicationOrderStatus.COMPLETED);
                break;
            case "draft":
                medicationOrder.setStatus(MedicationOrder.MedicationOrderStatus.DRAFT);
                break;
            case "entered_in_error":
                medicationOrder.setStatus(MedicationOrder.MedicationOrderStatus.ENTEREDINERROR);
                break;
            case "on_hold":
                medicationOrder.setStatus(MedicationOrder.MedicationOrderStatus.ONHOLD);
                break;
            case "stopped":
                medicationOrder.setStatus(MedicationOrder.MedicationOrderStatus.STOPPED);
                break;
        }

        if (medicationOrderDetails.getPatientId() != null) {
            medicationOrder.setPatient(new Reference("Patient/"+medicationOrderDetails.getPatientId()));
        } else {
            medicationOrder.setPatient(new Reference());
        }

        medicationOrder.setPrescriber(new Reference("Practitioner/"+medicationOrderDetails.getAutherId()));
        medicationOrder.setMedication(new Reference("Medication/"+medicationOrderDetails.getMedicationId()));
        medicationOrder.addDosageInstruction().setText(medicationOrderDetails.getDosageText());

        MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequest = new MedicationOrder.MedicationOrderDispenseRequestComponent();
      //  dispenseRequest.addUndeclaredExtension(new ExtensionDt(false, SystemURL.SD_EXTENSION_MEDICATION_QUANTITY_TEXT, new StringDt(medicationOrderDetails.getDispenseQuantityText())));
      //  dispenseRequest.addUndeclaredExtension(new ExtensionDt(true, SystemURL.SD_EXTENSION_PERSCRIPTION_REPEAT_REVIEW_DATE, new DateTimeDt(medicationOrderDetails.getDispenseReviewDate())));
        dispenseRequest.setMedication(new Reference("Medication/"+medicationOrderDetails.getDispenseMedicationId()));
        dispenseRequest.setNumberOfRepeatsAllowed(medicationOrderDetails.getDispenseRepeatsAllowed());
        medicationOrder.setDispenseRequest(dispenseRequest);

        return medicationOrder;
    }
}
