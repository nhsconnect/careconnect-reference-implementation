package uk.nhs.careconnect.ri.provider.medications;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.valueset.MedicationDispenseStatusEnum;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.SystemURL;
import uk.nhs.careconnect.ri.entity.medication.dispense.MedicationDispenseSearch;
import uk.nhs.careconnect.ri.model.medication.MedicationDispenseDetail;

import java.util.ArrayList;
import java.util.Collections;
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
                        medicationDispense.setStatus(MedicationDispenseStatusEnum.COMPLETED);
                        break;
                    case "entered_in_error":
                        medicationDispense.setStatus(MedicationDispenseStatusEnum.ENTERED_IN_ERROR);
                        break;
                    case "in_progress":
                        medicationDispense.setStatus(MedicationDispenseStatusEnum.IN_PROGRESS);
                        break;
                    case "on_hold":
                        medicationDispense.setStatus(MedicationDispenseStatusEnum.ON_HOLD);
                        break;
                    case "stopped":
                        medicationDispense.setStatus(MedicationDispenseStatusEnum.STOPPED);
                        break;
                }

                medicationDispense.setPatient(new ResourceReferenceDt("Patient/"+patientId));
                medicationDispense.setAuthorizingPrescription(Collections.singletonList(new ResourceReferenceDt("MedicationOrder/"+medicationDispenseDetail.getMedicationOrderId())));

                Medication medication = new Medication();
                CodingDt coding = new CodingDt();
                coding.setSystem(SystemURL.SNOMED);
                coding.setCode(String.valueOf(medicationDispenseDetail.getMedicationId()));
                coding.setDisplay(medicationDispenseDetail.getMedicationName());
                CodeableConceptDt codeableConcept = new CodeableConceptDt();
                codeableConcept.setCoding(Collections.singletonList(coding));
                medication.setCode(codeableConcept);

                medicationDispense.addDosageInstruction().setText(medicationDispenseDetail.getDosageText());
                medicationDispenses.add(medicationDispense);
            }
        }

        return medicationDispenses;
    }
}
