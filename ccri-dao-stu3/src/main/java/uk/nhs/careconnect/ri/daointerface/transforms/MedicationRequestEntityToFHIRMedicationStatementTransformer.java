package uk.nhs.careconnect.ri.daointerface.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.BaseAddress;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestDosage;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestEntity;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestIdentifier;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;


@Component
public class MedicationRequestEntityToFHIRMedicationStatementTransformer implements Transformer<MedicationRequestEntity, MedicationStatement> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    public MedicationRequestEntityToFHIRMedicationStatementTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public MedicationStatement transform(final MedicationRequestEntity medicationRequestEntity) {
        final MedicationStatement medicationStatement = new MedicationStatement();

        Meta meta = new Meta().addProfile(CareConnectProfile.MedicationStatement_1);

        if (medicationRequestEntity.getUpdated() != null) {
            meta.setLastUpdated(medicationRequestEntity.getUpdated());
        }
        else {
            if (medicationRequestEntity.getCreated() != null) {
                meta.setLastUpdated(medicationRequestEntity.getCreated());
            }
        }
        medicationStatement.setMeta(meta);

        for(MedicationRequestIdentifier identifier : medicationRequestEntity.getIdentifiers())
        {
            medicationStatement.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }
        if (medicationRequestEntity.getPatient() != null) {
            medicationStatement.setSubject(new Reference("Patient/"+medicationRequestEntity.getPatient().getId()));
        }

        if (medicationRequestEntity.getCategoryCode() != null) {
            medicationStatement.getCategory().addCoding()
                    .setDisplay(medicationRequestEntity.getCategoryCode().getDisplay())
                    .setSystem(medicationRequestEntity.getCategoryCode().getSystem())
                    .setCode(medicationRequestEntity.getCategoryCode().getCode());
        }
        if (medicationRequestEntity.getContextEpisodeOfCare() != null) {
            medicationStatement.setContext(new Reference("EpisodeOfCare/"+medicationRequestEntity.getContextEpisodeOfCare().getId()));
        }
        if (medicationRequestEntity.getContextEncounter() != null) {
            medicationStatement.setContext(new Reference("Encounter/"+medicationRequestEntity.getContextEncounter().getId()));
        }
        if (medicationRequestEntity.getAuthoredDate() !=null) {
            medicationStatement.setEffective(new DateType(medicationRequestEntity.getAuthoredDate()));
        }

        // Typo 15/12/2017 KGM Was MedicationOrder
        medicationStatement.addDerivedFrom().setReference("MedicationRequest/"+medicationRequestEntity.getId());

        medicationStatement.setTaken(MedicationStatement.MedicationStatementTaken.Y);

        if (medicationRequestEntity.getStatus() != null) {
            switch (medicationRequestEntity.getStatus()) {


                case ACTIVE:
                    medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);
                    break;
                case ONHOLD:
                    medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.ONHOLD);
                    break;
                case STOPPED:
                    medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.STOPPED);
                    break;

                case COMPLETED:
                    medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);
                    break;

                case DRAFT:
                    medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.INTENDED);
                    break;
                case NULL:
                    medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.NULL);
                    break;
                case UNKNOWN:
                case CANCELLED:
                case ENTEREDINERROR:
                    medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.ENTEREDINERROR);
                    break;
            }
        }



        if (medicationRequestEntity.getMedicationCode() != null) {

                CodeableConcept medication = new CodeableConcept();
                medication.addCoding()
                    .setDisplay(medicationRequestEntity.getMedicationCode().getDisplay())
                    .setSystem(medicationRequestEntity.getMedicationCode().getSystem())
                    .setCode(medicationRequestEntity.getMedicationCode().getCode());
                medicationStatement.setMedication(medication);

        }
        if (medicationRequestEntity.getReasonCode() != null) {
            medicationStatement.addReasonCode().addCoding()
                    .setDisplay(medicationRequestEntity.getReasonCode().getDisplay())
                    .setSystem(medicationRequestEntity.getReasonCode().getSystem())
                    .setCode(medicationRequestEntity.getReasonCode().getCode());
        }
        if (medicationRequestEntity.getReasonCondition() != null) {
            medicationStatement.addReasonReference(new Reference(("Condition/"+medicationRequestEntity.getReasonCondition().getId())));
        }

        if (medicationRequestEntity.getReasonObservation() != null) {
            medicationStatement.addReasonReference(new Reference(("Observation/"+medicationRequestEntity.getReasonObservation().getId())));
        }

        for (MedicationRequestDosage dosageEntity : medicationRequestEntity.getDosages()) {
            Dosage dosage = medicationStatement.addDosage();
            if (dosageEntity.getAsNeededBoolean() != null) {
                dosage.setAsNeeded(new BooleanType().setValue(dosageEntity.getAsNeededBoolean()));
            }
            if (dosageEntity.getAdditionalInstructionCode() != null) {
                dosage.addAdditionalInstruction().addCoding()
                        .setDisplay(dosageEntity.getAdditionalInstructionCode().getDisplay())
                        .setSystem(dosageEntity.getAdditionalInstructionCode().getSystem())
                        .setCode(dosageEntity.getAdditionalInstructionCode().getCode());
            }
            if (dosageEntity.getAsNeededCode()!=null) {
                CodeableConcept concept = new CodeableConcept();
                concept.addCoding() .setDisplay(dosageEntity.getAsNeededCode().getDisplay())
                        .setSystem(dosageEntity.getAsNeededCode().getSystem())
                        .setCode(dosageEntity.getAsNeededCode().getCode());
                dosage.setAsNeeded(concept);
            }
            if (dosageEntity.getMethodCode()!=null) {
                dosage.getMethod().addCoding()
                        .setDisplay(dosageEntity.getMethodCode().getDisplay())
                        .setSystem(dosageEntity.getMethodCode().getSystem())
                        .setCode(dosageEntity.getMethodCode().getCode());
            }
            if (dosageEntity.getSiteCode()!=null) {
                dosage.getSite().addCoding()
                        .setDisplay(dosageEntity.getSiteCode().getDisplay())
                        .setSystem(dosageEntity.getSiteCode().getSystem())
                        .setCode(dosageEntity.getSiteCode().getCode());
            }
            if (dosageEntity.getRouteCode()!=null) {
                dosage.getRoute().addCoding()
                        .setDisplay(dosageEntity.getRouteCode().getDisplay())
                        .setSystem(dosageEntity.getRouteCode().getSystem())
                        .setCode(dosageEntity.getRouteCode().getCode());
            }
            if (dosageEntity.getPatientInstruction() != null) {
                dosage.setPatientInstruction(dosageEntity.getPatientInstruction());
            }
            if (dosageEntity.getOtherText() != null) {
                dosage.setText(dosageEntity.getOtherText());
            }
            if (dosageEntity.getDoseRangeLow() != null || dosageEntity.getDoseRangeHigh() != null) {
                Range range = new Range();
                if (dosageEntity.getDoseRangeLow() != null) {
                    SimpleQuantity qty = new SimpleQuantity();
                    qty.setValue(dosageEntity.getDoseRangeLow());
                    if (dosageEntity.getDoseLowUnitOfMeasure() != null) {
                        qty.setCode(dosageEntity.getDoseLowUnitOfMeasure().getCode());
                        qty.setSystem(dosageEntity.getDoseLowUnitOfMeasure().getSystem());
                        qty.setUnit(dosageEntity.getDoseLowUnitOfMeasure().getDisplay());
                    }
                    range.setLow(qty);
                }
                if (dosageEntity.getDoseRangeHigh() != null) {
                    SimpleQuantity qty = new SimpleQuantity();
                    qty.setValue(dosageEntity.getDoseRangeHigh());
                    if (dosageEntity.getDoseHighUnitOfMeasure() != null) {
                        qty.setCode(dosageEntity.getDoseHighUnitOfMeasure().getCode());
                        qty.setSystem(dosageEntity.getDoseHighUnitOfMeasure().getSystem());
                        qty.setUnit(dosageEntity.getDoseHighUnitOfMeasure().getDisplay());
                    }
                    range.setHigh(qty);
                }
                dosage.setDose(range);
            } else {
                if (dosageEntity.getDoseQuantity() != null) {
                    SimpleQuantity qty = new SimpleQuantity();
                    qty.setValue(dosageEntity.getDoseQuantity());
                    if (dosageEntity.getDoseUnitOfMeasure() != null) {
                        qty.setCode(dosageEntity.getDoseUnitOfMeasure().getCode());
                        qty.setSystem(dosageEntity.getDoseUnitOfMeasure().getSystem());
                        qty.setUnit(dosageEntity.getDoseUnitOfMeasure().getDisplay());
                    }
                    dosage.setDose(qty);
                }
            }

        }

        medicationStatement.setId(medicationRequestEntity.getId().toString());

        return medicationStatement;

    }
}
