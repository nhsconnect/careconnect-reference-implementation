package uk.nhs.careconnect.ri.provider.medications;


import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.MedicationAdministration;
import org.hl7.fhir.instance.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.medication.administration.MedicationAdministrationSearch;
import uk.nhs.careconnect.ri.model.medication.MedicationAdministrationDetail;

import java.util.ArrayList;
import java.util.List;

@Component
public class MedicationAdministrationResourceProvider  implements IResourceProvider {

    @Autowired
    private MedicationAdministrationSearch medicationAdministrationSearch;

    @Override
    public Class<MedicationAdministration> getResourceType() {
        return MedicationAdministration.class;
    }

    @Search
    public List<MedicationAdministration> getMedicationAdministrationsForPatientId(@RequiredParam(name = "patient") String patientId) {
        ArrayList<MedicationAdministration> medicationAdministrations = new ArrayList<>();

        List<MedicationAdministrationDetail> medicationAdministrationDetailList = medicationAdministrationSearch.findMedicationAdministrationForPatient(Long.parseLong(patientId));

        if (medicationAdministrationDetailList != null && !medicationAdministrationDetailList.isEmpty()) {
            for(MedicationAdministrationDetail medicationAdministrationDetail : medicationAdministrationDetailList) {
                MedicationAdministration medicationAdministration = new MedicationAdministration();
                medicationAdministration.setId(String.valueOf(medicationAdministrationDetail.getId()));
                medicationAdministration.getMeta().setLastUpdated(medicationAdministrationDetail.getLastUpdated());
                medicationAdministration.getMeta().setVersionId(String.valueOf(medicationAdministrationDetail.getLastUpdated().getTime()));
                medicationAdministration.setPatient(new Reference("Patient/"+medicationAdministrationDetail.getPatientId()));
                medicationAdministration.setPractitioner(new Reference("Practitioner/"+medicationAdministrationDetail.getPractitionerId()));
                medicationAdministration.setPrescription(new Reference("MedicationOrder/"+medicationAdministrationDetail.getPrescriptionId()));
                medicationAdministration.setEffectiveTime(new DateTimeType(medicationAdministrationDetail.getAdministrationDate()));
                medicationAdministration.setMedication(new Reference("Medication/"+medicationAdministrationDetail.getMedicationId()));
                medicationAdministrations.add(medicationAdministration);
            }
        }

        return medicationAdministrations;
    }
}
