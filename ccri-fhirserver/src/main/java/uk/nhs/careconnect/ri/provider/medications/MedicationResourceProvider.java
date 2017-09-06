package uk.nhs.careconnect.ri.provider.medications;


import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.instance.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.medications.MedicationEntity;
import uk.nhs.careconnect.ri.entity.medications.MedicationRepository;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

@Component
public class MedicationResourceProvider implements IResourceProvider {

    @Autowired
    private MedicationRepository medicationRepository;

    @Override
    public Class<Medication> getResourceType() {
        return Medication.class;
    }

    @Read()
    public Medication getMedicationById(@IdParam IdType medicationId) {
        MedicationEntity medicationEntity = medicationRepository.findOne(medicationId.getIdPartAsLong());

        if (medicationEntity == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
           // TODO  operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No medication details found for ID: " + medicationId.getIdPart());
            throw new InternalErrorException("No medication details found for ID: " + medicationId.getIdPart(), operationalOutcome);
        }

        Coding coding = new Coding().setSystem(CareConnectSystem.SNOMEDCT).setCode(String.valueOf(medicationEntity.getId())).setDisplay(medicationEntity.getName());
        CodeableConcept codableConcept = new CodeableConcept();
        codableConcept.addCoding(coding);

        Medication medication = new Medication().setCode(codableConcept);
        medication.setId(String.valueOf(medicationEntity.getId()));
        medication.getMeta().setLastUpdated(medicationEntity.getLastUpdated());
        medication.getMeta().setVersionId(String.valueOf(medicationEntity.getLastUpdated().getTime()));

        return medication;
    }
}
