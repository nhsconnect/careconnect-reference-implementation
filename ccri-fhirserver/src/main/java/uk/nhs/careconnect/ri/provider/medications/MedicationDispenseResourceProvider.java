package uk.nhs.careconnect.ri.provider.medications;


import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.medication.dispense.MedicationDispenseSearch;
import uk.nhs.careconnect.ri.model.medication.MedicationDispenseDetail;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class MedicationDispenseResourceProvider implements IResourceProvider {

    @Autowired
    private MedicationDispenseSearch medicationDispenseSearch;

    @Override
    public Class<MedicationDispense> getResourceType() {
        return MedicationDispense.class;
    }

    @Search
    public List<MedicationDispense> getMedicationDispensesForPatientId(@RequiredParam(name = "patient") String patientId) {
        ArrayList<MedicationDispense> medicationDispenses = new ArrayList<>();

        List<MedicationDispenseDetail> medicationDispenseDetailList = medicationDispenseSearch.findMedicationDispenseForPatient(Long.parseLong(patientId));

        if (medicationDispenseDetailList != null && !medicationDispenseDetailList.isEmpty()) {
            for (MedicationDispenseDetail medicationDispenseDetail : medicationDispenseDetailList) {
                MedicationDispense medicationDispense = new MedicationDispense();
                medicationDispense.setId(String.valueOf(medicationDispenseDetail.getId()));
                medicationDispense.getMeta().setLastUpdated(medicationDispenseDetail.getLastUpdated());
                medicationDispense.getMeta().setVersionId(String.valueOf(medicationDispenseDetail.getLastUpdated().getTime()));

                switch (medicationDispenseDetail.getStatus().toLowerCase(Locale.UK)) {
                    case "completed":
                        medicationDispense.setStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED);
                        break;
                    case "entered_in_error":
                        medicationDispense.setStatus(MedicationDispense.MedicationDispenseStatus.ENTEREDINERROR);
                        break;
                    case "in_progress":
                        medicationDispense.setStatus(MedicationDispense.MedicationDispenseStatus.INPROGRESS);
                        break;
                    case "on_hold":
                        medicationDispense.setStatus(MedicationDispense.MedicationDispenseStatus.ONHOLD);
                        break;
                    case "stopped":
                        medicationDispense.setStatus(MedicationDispense.MedicationDispenseStatus.STOPPED);
                        break;
                }

                medicationDispense.setPatient(new Reference("Patient/"+patientId));
                medicationDispense.addAuthorizingPrescription(new Reference("MedicationOrder/"+medicationDispenseDetail.getMedicationOrderId()));

                Medication medication = new Medication();
                Coding coding = new Coding();
                coding.setSystem(CareConnectSystem.SNOMEDCT);
                coding.setCode(String.valueOf(medicationDispenseDetail.getMedicationId()));
                coding.setDisplay(medicationDispenseDetail.getMedicationName());
                CodeableConcept codeableConcept = new CodeableConcept();
                codeableConcept.addCoding(coding);
                medication.setCode(codeableConcept);

                medicationDispense.addDosageInstruction().setText(medicationDispenseDetail.getDosageText());
                medicationDispenses.add(medicationDispense);
            }
        }

        return medicationDispenses;
    }
}
