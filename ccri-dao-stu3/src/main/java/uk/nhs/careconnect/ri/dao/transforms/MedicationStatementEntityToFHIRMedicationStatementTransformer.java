package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.*;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.*;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;


@Component
public class MedicationStatementEntityToFHIRMedicationStatementTransformer implements Transformer<MedicationStatementEntity, MedicationStatement> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationStatementEntityToFHIRMedicationStatementTransformer.class);


    public MedicationStatementEntityToFHIRMedicationStatementTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public MedicationStatement transform(final MedicationStatementEntity medicationStatementEntity)  {


        final MedicationStatement medicationStatement = new MedicationStatement();
        try {
            Meta meta = new Meta().addProfile(CareConnectProfile.MedicationStatement_1);

            if (medicationStatementEntity.getUpdated() != null) {
                meta.setLastUpdated(medicationStatementEntity.getUpdated());
            } else {
                if (medicationStatementEntity.getCreated() != null) {
                    meta.setLastUpdated(medicationStatementEntity.getCreated());
                }
            }
            medicationStatement.setMeta(meta);

            for (MedicationStatementIdentifier identifier : medicationStatementEntity.getIdentifiers()) {
                medicationStatement.addIdentifier()
                        .setSystem(identifier.getSystem().getUri())
                        .setValue(identifier.getValue());
            }
            if (medicationStatementEntity.getPatient() != null) {
                medicationStatement.setSubject(new Reference("Patient/" + medicationStatementEntity.getPatient().getId()));
            }

            if (medicationStatementEntity.getCategoryCode() != null) {
                medicationStatement.getCategory().addCoding()
                        .setDisplay(medicationStatementEntity.getCategoryCode().getDisplay())
                        .setSystem(medicationStatementEntity.getCategoryCode().getSystem())
                        .setCode(medicationStatementEntity.getCategoryCode().getCode());
            }

            if (medicationStatementEntity.getContextEncounter() != null) {
                medicationStatement.setContext(new Reference("Encounter/" + medicationStatementEntity.getContextEncounter().getId()));
            }
            if (medicationStatementEntity.getAssertedDate() != null) {
                medicationStatement.setDateAsserted(medicationStatementEntity.getAssertedDate());
            }

            if (medicationStatementEntity.getEffectiveStartDate() != null) {
                if (medicationStatementEntity.getEffectiveEndDate() != null) {
                    Period period = new Period();
                    period.setStart(medicationStatementEntity.getEffectiveStartDate());
                    period.setEnd(medicationStatementEntity.getEffectiveEndDate());
                    medicationStatement.setEffective(period);
                } else {
                    medicationStatement.setEffective(new DateTimeType().setValue(medicationStatementEntity.getEffectiveStartDate()));
                }
            }

            if (medicationStatementEntity.getNote() != null) {

                medicationStatement.addNote().setText(medicationStatementEntity.getNote());
            }

            // Typo 15/12/2017 KGM Was MedicationOrder
            for (MedicationStatementDerivedFrom
                    derivedFrom : medicationStatementEntity.getDerives()) {
               // NOT YET IMPLEMENTED
            }
            for (MedicationStatementBasedOn
                    basedOn : medicationStatementEntity.getBasedOn()) {
                if (basedOn.getPrescription() != null) {
                    medicationStatement.getBasedOn().add(new Reference("MedicationRequest/"+basedOn.getPrescription().getId()));
                }
            }
            for (MedicationStatementReason
                    reason : medicationStatementEntity.getReasons()) {
                if (reason.getCondition() != null) {
                    medicationStatement.getReasonReference().add(new Reference("Condition/"+reason.getCondition().getId()));
                }
                if (reason.getObservation() != null) {
                    medicationStatement.getReasonReference().add(new Reference("Observation/"+reason.getObservation().getId()));
                }
            }
            for (MedicationStatementPartOf
                    partOf : medicationStatementEntity.getPartOfs()) {
                if (partOf.getObservation() != null) {
                    medicationStatement.getPartOf().add(new Reference("Observation/"+partOf.getObservation().getId()));
                }
                if (partOf.getProcedure() != null) {
                    medicationStatement.getPartOf().add(new Reference("Procedure/"+partOf.getProcedure().getId()));
                }
            }

            medicationStatement.setTaken(MedicationStatement.MedicationStatementTaken.Y);

            if (medicationStatementEntity.getStatus() != null) {
               medicationStatement.setStatus(medicationStatementEntity.getStatus());
            }

            if (medicationStatementEntity.getMedicationEntity() != null) {
                medicationStatement.setMedication(new Reference("Medication/"+medicationStatementEntity.getMedicationEntity().getId())
                        .setDisplay(medicationStatementEntity.getMedicationEntity().getMedicationCode().getDisplay()));
            }

            if (medicationStatementEntity.getNotTakenCode() != null) {
                CodeableConcept code = new CodeableConcept();
                code.addCoding()
                        .setCode(medicationStatementEntity.getNotTakenCode().getCode())
                        .setSystem(medicationStatementEntity.getNotTakenCode().getDisplay())
                        .setDisplay(medicationStatementEntity.getNotTakenCode().getDisplay());
                medicationStatement.getReasonNotTaken().add(code);
            }
            if (medicationStatementEntity.getTaken() != null) {
                medicationStatement.setTaken(medicationStatementEntity.getTaken());
            }

            for (MedicationStatementDosage dosageEntity : medicationStatementEntity.getDosages()) {
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
                if (dosageEntity.getAsNeededCode() != null) {
                    CodeableConcept concept = new CodeableConcept();
                    concept.addCoding().setDisplay(dosageEntity.getAsNeededCode().getDisplay())
                            .setSystem(dosageEntity.getAsNeededCode().getSystem())
                            .setCode(dosageEntity.getAsNeededCode().getCode());
                    dosage.setAsNeeded(concept);
                }
                if (dosageEntity.getMethodCode() != null) {
                    dosage.getMethod().addCoding()
                            .setDisplay(dosageEntity.getMethodCode().getDisplay())
                            .setSystem(dosageEntity.getMethodCode().getSystem())
                            .setCode(dosageEntity.getMethodCode().getCode());
                }
                if (dosageEntity.getSiteCode() != null) {
                    dosage.getSite().addCoding()
                            .setDisplay(dosageEntity.getSiteCode().getDisplay())
                            .setSystem(dosageEntity.getSiteCode().getSystem())
                            .setCode(dosageEntity.getSiteCode().getCode());
                }
                if (dosageEntity.getRouteCode() != null) {
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

            medicationStatement.setId(medicationStatementEntity.getId().toString());
        }
        catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return medicationStatement;

    }
}
