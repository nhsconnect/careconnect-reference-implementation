package uk.gov.hscic.medication.orders;

import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.medication.MedicationOrderDetails;

public class MedicationOrderEntityToMedicationOrderDetailsTransformer implements Transformer<MedicationOrderEntity, MedicationOrderDetails> {

    @Override
    public MedicationOrderDetails transform(MedicationOrderEntity item) {
        MedicationOrderDetails medicationOrderDetails = new MedicationOrderDetails();
        medicationOrderDetails.setId(item.getId());
        medicationOrderDetails.setDateWritten(item.getDateWritten());
        medicationOrderDetails.setOrderStatus(item.getOrderStatus());
        medicationOrderDetails.setPatientId(item.getPatientId());
        medicationOrderDetails.setAutherId(item.getAutherId());
        medicationOrderDetails.setMedicationId(item.getMedicationId());
        medicationOrderDetails.setDosageText(item.getDosageText());
        medicationOrderDetails.setDispenseQuantityText(item.getDispenseQuantityText());
        medicationOrderDetails.setDispenseReviewDate(item.getDispenseReviewDate());
        medicationOrderDetails.setDispenseMedicationId(item.getDispenseMedicationId());
        medicationOrderDetails.setDispenseRepeatsAllowed(item.getDispenseRepeatsAllowed());
        medicationOrderDetails.setLastUpdated(item.getLastUpdated());
        return medicationOrderDetails;
    }
}
