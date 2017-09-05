package uk.nhs.careconnect.ri.provider.medications;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.SystemURL;
import uk.nhs.careconnect.ri.entity.medications.MedicationEntity;
import uk.nhs.careconnect.ri.entity.medications.MedicationRepository;

@Component
public class MedicationResourceProvider implements IResourceProvider {

    @Autowired
    private MedicationRepository medicationRepository;

    @Override
    public Class<Medication> getResourceType() {
        return Medication.class;
    }

    @Read()
    public Medication getMedicationById(@IdParam IdDt medicationId) {
        MedicationEntity medicationEntity = medicationRepository.findOne(medicationId.getIdPartAsLong());

        if (medicationEntity == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
            operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No medication details found for ID: " + medicationId.getIdPart());
            throw new InternalErrorException("No medication details found for ID: " + medicationId.getIdPart(), operationalOutcome);
        }

        CodingDt coding = new CodingDt(SystemURL.SNOMED, String.valueOf(medicationEntity.getId())).setDisplay(medicationEntity.getName());
        CodeableConceptDt codableConcept = new CodeableConceptDt();
        codableConcept.addCoding(coding);

        Medication medication = new Medication().setCode(codableConcept);
        medication.setId(String.valueOf(medicationEntity.getId()));
        medication.getMeta().setLastUpdated(medicationEntity.getLastUpdated());
        medication.getMeta().setVersionId(String.valueOf(medicationEntity.getLastUpdated().getTime()));

        return medication;
    }
}
