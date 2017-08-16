package uk.gov.hscic.medication.administration;

import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.medication.MedicationAdministrationDetail; 

public class MedicationAdministrationEntityToMedicationAdministrationDetailTransformer implements Transformer<MedicationAdministrationEntity, MedicationAdministrationDetail> {

    @Override
    public MedicationAdministrationDetail transform(MedicationAdministrationEntity item) {
        MedicationAdministrationDetail administrationDetail = new MedicationAdministrationDetail();
        administrationDetail.setId(item.getId());
        administrationDetail.setPatientId(item.getPatientId());
        administrationDetail.setPractitionerId(item.getPractitionerId());
        administrationDetail.setEncounterId(item.getEncounterId());
        administrationDetail.setPrescriptionId(item.getPrescriptionId());
        administrationDetail.setAdministrationDate(item.getAdministrationDate());
        administrationDetail.setMedicationId(item.getMedicationId());
        administrationDetail.setLastUpdated(item.getLastUpdated());
        return administrationDetail;
    }
}
