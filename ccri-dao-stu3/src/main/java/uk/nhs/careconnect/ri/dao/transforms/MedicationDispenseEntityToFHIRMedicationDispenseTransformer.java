package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseDosage;
import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseIdentifier;


@Component
public class MedicationDispenseEntityToFHIRMedicationDispenseTransformer implements Transformer<MedicationDispenseEntity, MedicationDispense> {

   

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationDispenseEntityToFHIRMedicationDispenseTransformer.class);

    
    @Override
    public MedicationDispense transform(final MedicationDispenseEntity medicationDispenseEntity) {
        final MedicationDispense medicationDispense = new MedicationDispense();
    try {
        Meta meta = new Meta();

        if (medicationDispenseEntity.getUpdated() != null) {
            meta.setLastUpdated(medicationDispenseEntity.getUpdated());
        }
        else {
            if (medicationDispenseEntity.getCreated() != null) {
                meta.setLastUpdated(medicationDispenseEntity.getCreated());
            }
        }
        medicationDispense.setMeta(meta);

        for(MedicationDispenseIdentifier identifier : medicationDispenseEntity.getIdentifiers())
        {
            medicationDispense.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        // part of

        if (medicationDispenseEntity.getStatus() !=null) {
            medicationDispense.setStatus(medicationDispenseEntity.getStatus());
        }
        if (medicationDispenseEntity.getCategoryCode() != null) {
            medicationDispense.getCategory().addCoding()
                    .setDisplay(medicationDispenseEntity.getCategoryCode().getDisplay())
                    .setSystem(medicationDispenseEntity.getCategoryCode().getSystem())
                    .setCode(medicationDispenseEntity.getCategoryCode().getCode());
        }
        if (medicationDispenseEntity.getMedicationEntity() != null) {
            medicationDispense.setMedication(new Reference("Medication/"+medicationDispenseEntity.getMedicationEntity().getId())
                    .setDisplay(medicationDispenseEntity.getMedicationEntity().getMedicationCode().getDisplay()));
        }

        if (medicationDispenseEntity.getPatient() != null) {
            medicationDispense.setSubject(new Reference("Patient/"+medicationDispenseEntity.getPatient().getId()));
        }

        if (medicationDispenseEntity.getContextEpisodeOfCare() != null) {
            medicationDispense.setContext(new Reference("EpisodeOfCare/"+medicationDispenseEntity.getContextEpisodeOfCare().getId()));
        }
        if (medicationDispenseEntity.getContextEncounter() != null) {
            medicationDispense.setContext(new Reference("Encounter/"+medicationDispenseEntity.getContextEncounter().getId()));
        }

        if (medicationDispenseEntity.getPerformerPractitioner() != null) {
            if (medicationDispense.getPerformer().size() == 0) medicationDispense.addPerformer();
            medicationDispense.getPerformerFirstRep().setActor(new Reference("Practitioner/"+medicationDispenseEntity.getPerformerPractitioner().getId()).setDisplay(medicationDispenseEntity.getPerformerPractitioner().getNames().get(0).getDisplayName()));
        }
        if (medicationDispenseEntity.getPerformerOrganisation() != null) {
            if (medicationDispense.getPerformer().size() == 0) medicationDispense.addPerformer();
            medicationDispense.getPerformerFirstRep().setOnBehalfOf(new Reference("Organization/"+medicationDispenseEntity.getPerformerOrganisation().getId()).setDisplay(medicationDispenseEntity.getPerformerOrganisation().getName()));
        }
        if (medicationDispenseEntity.getPrescription() != null) {
            medicationDispense.addAuthorizingPrescription(new Reference("MedicationRequest/"+medicationDispenseEntity.getPrescription().getId()));
        }
        if (medicationDispenseEntity.getTypeCode() != null) {
            medicationDispense.getType().addCoding()
                    .setDisplay(medicationDispenseEntity.getTypeCode().getDisplay())
                    .setSystem(medicationDispenseEntity.getTypeCode().getSystem())
                    .setCode(medicationDispenseEntity.getTypeCode().getCode());
        }

        // quantity

        if (medicationDispenseEntity.getQuantityValue() != null) {
            medicationDispense.getQuantity().setValue(medicationDispenseEntity.getQuantityValue());
            medicationDispense.getQuantity().setUnit((medicationDispenseEntity.getQuantityUnit()));
        }

        if (medicationDispenseEntity.getDaysSupplyValue() != null) {
            medicationDispense.getDaysSupply().setValue(medicationDispenseEntity.getDaysSupplyValue());
            medicationDispense.getDaysSupply().setUnit((medicationDispenseEntity.getDaysSupplyUnit()));
        }

        if (medicationDispenseEntity.getWhenHandedOver() !=null) {
            medicationDispense.setWhenHandedOver(medicationDispenseEntity.getWhenHandedOver());
        }
        if (medicationDispenseEntity.getWhenPrepared() !=null) {
            medicationDispense.setWhenPrepared(medicationDispenseEntity.getWhenPrepared());
        }

        if (medicationDispenseEntity.getReceiverPractitioner() != null) {
            medicationDispense.addReceiver(new Reference("Practitioner/"+medicationDispenseEntity.getReceiverPractitioner().getId()).setDisplay(medicationDispenseEntity.getReceiverPractitioner().getNames().get(0).getDisplayName()));
        }
        if (medicationDispenseEntity.getReceiverOrganisaton() != null) {
            medicationDispense.addReceiver(new Reference("Organization/"+medicationDispenseEntity.getReceiverOrganisaton().getId()).setDisplay(medicationDispenseEntity.getReceiverOrganisaton().getName()));
        }

        if (medicationDispenseEntity.getLocation() !=null) {
            medicationDispense.setDestination(new Reference("Location/"+medicationDispenseEntity.getLocation().getId()));
        }

        if (medicationDispenseEntity.getSubstituted() !=null) {
            medicationDispense.getSubstitution().setWasSubstituted(medicationDispenseEntity.getSubstituted());

            if (medicationDispenseEntity.getSubstitutionTypeCode() != null) {
                medicationDispense.getSubstitution().getType().addCoding()
                        .setDisplay(medicationDispenseEntity.getSubstitutionTypeCode().getDisplay())
                        .setSystem(medicationDispenseEntity.getSubstitutionTypeCode().getSystem())
                        .setCode(medicationDispenseEntity.getSubstitutionTypeCode().getCode());
            }
            if (medicationDispenseEntity.getSubstitutionReasonCode() != null) {
                medicationDispense.getSubstitution().addReason().addCoding()
                        .setDisplay(medicationDispenseEntity.getSubstitutionReasonCode().getDisplay())
                        .setSystem(medicationDispenseEntity.getSubstitutionReasonCode().getSystem())
                        .setCode(medicationDispenseEntity.getSubstitutionReasonCode().getCode());
            }
            if (medicationDispenseEntity.getSubstitutionPractitioner() != null) {
                medicationDispense.getSubstitution().addResponsibleParty(new Reference("Practitioner/"+medicationDispenseEntity.getSubstitutionPractitioner().getId()).setDisplay(medicationDispenseEntity.getSubstitutionPractitioner().getNames().get(0).getDisplayName()));
            }
        }

        if (medicationDispenseEntity.getNotDone() != null) {
            medicationDispense.setNotDone(medicationDispenseEntity.getNotDone());
        }

        if (medicationDispenseEntity.getNotDoneCode() != null) {
            medicationDispense.getNotDoneReasonCodeableConcept().addCoding()
                    .setDisplay(medicationDispenseEntity.getNotDoneCode().getDisplay())
                    .setSystem(medicationDispenseEntity.getNotDoneCode().getSystem())
                    .setCode(medicationDispenseEntity.getNotDoneCode().getCode());
        }

       
        for (MedicationDispenseDosage dosageEntity : medicationDispenseEntity.getDosageInstructions()) {
            Dosage dosage = medicationDispense.addDosageInstruction();
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

        medicationDispense.setId(medicationDispenseEntity.getId().toString());
    }
    catch (Exception ex) {
        log.error(ex.getMessage());
    }
        return medicationDispense;

    }
}
