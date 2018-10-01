package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;

import org.springframework.stereotype.Component;

import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;

import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;



@Component
public class MedicationEntityToFHIRMedicationTransformer implements Transformer<MedicationEntity, Medication> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationEntityToFHIRMedicationTransformer.class);


    @Override
    public Medication transform(final MedicationEntity medicationEntity) {

        // TODO Move to transf
        Medication medication = new Medication();

        Meta meta = new Meta().addProfile(CareConnectProfile.Medication_1);

        if (medicationEntity.getUpdated() != null) {
            meta.setLastUpdated(medicationEntity.getUpdated());
        }
        else {
            if (medicationEntity.getCreated() != null) {
                meta.setLastUpdated(medicationEntity.getCreated());
            }
        }
        medication.setMeta(meta);

        medication.setId(medicationEntity.getId().toString());
        medication.getCode()
                .addCoding()
                .setCode(medicationEntity.getMedicationCode().getCode())
                .setSystem(medicationEntity.getMedicationCode().getSystem())
                .setDisplay(medicationEntity.getMedicationCode().getDisplay());
        return medication;

    }
}
