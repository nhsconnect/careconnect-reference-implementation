package uk.nhs.careconnect.ri.lib.gateway.transform;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;


@Component
public class FHIRMedicationStatementToFHIRMedicationRequestTransformer implements Transformer<MedicationStatement, MedicationRequest> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FHIRMedicationStatementToFHIRMedicationRequestTransformer.class);


    @Override
    public MedicationRequest transform(final MedicationStatement medicationStatement) {

        // TODO Move to transf
        MedicationRequest medicationRequest = new MedicationRequest();
        Meta meta = new Meta();
        if (medicationStatement.getMeta() != null && medicationStatement.getMeta().getLastUpdated() != null) {
            meta.setLastUpdated(medicationStatement.getMeta().getLastUpdated());
        }
        else {

        }
        medicationRequest.setMeta(meta);

        medicationRequest.setId(medicationStatement.getId());

        medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

        if (medicationStatement.hasStatus()) {
            switch (medicationStatement.getStatus()) {
                case NULL:
                    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.NULL);
                    break;
                case ACTIVE:
                    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
                    break;
                case ONHOLD:
                    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ONHOLD);
                    break;
                case STOPPED:
                    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.STOPPED);
                    break;
                case INTENDED:
                    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.DRAFT);
                    medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.PLAN);
                    break;
                case ENTEREDINERROR:
                    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ENTEREDINERROR);
                    break;
            }
        }
        for (Identifier identifier : medicationStatement.getIdentifier()) {
            medicationRequest.addIdentifier(identifier);
        }
        if (medicationStatement.hasTaken()) {
            // Look into this, not currently supported in CCRI
        }
        if (medicationStatement.hasDosage()) {
            for (Dosage dosageInstruction : medicationStatement.getDosage()) {
                medicationRequest.addDosageInstruction(dosageInstruction);
            }
        }
        if (medicationStatement.hasMedicationReference()) {
            medicationRequest.setMedication(medicationStatement.getMedication());
        }
        if (medicationStatement.hasSubject()) {
            medicationRequest.setSubject(medicationStatement.getSubject());
        }

        return medicationRequest;

    }
}
