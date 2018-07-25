package uk.nhs.careconnect.ri.daointerface.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;

import org.springframework.stereotype.Component;

import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationRequestEntity;

import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;



@Component
public class MedicationRequestEntityToFHIRMedicationTransformer implements Transformer<MedicationRequestEntity, Medication> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationRequestEntityToFHIRMedicationTransformer.class);


    @Override
    public Medication transform(final MedicationRequestEntity medicationRequestEntity) {

        // TODO Move to transf
        Medication medication = new Medication();

        Meta meta = new Meta().addProfile(CareConnectProfile.Medication_1);

        if (medicationRequestEntity.getUpdated() != null) {
            meta.setLastUpdated(medicationRequestEntity.getUpdated());
        }
        else {
            if (medicationRequestEntity.getCreated() != null) {
                meta.setLastUpdated(medicationRequestEntity.getCreated());
            }
        }
        medication.setMeta(meta);

        medication.setId(medicationRequestEntity.getId().toString());
        medication.getCode()
                .addCoding()
                .setCode(medicationRequestEntity.getMedicationCode().getCode())
                .setSystem(medicationRequestEntity.getMedicationCode().getSystem())
                .setDisplay(medicationRequestEntity.getMedicationCode().getDisplay());
        return medication;

    }
}
