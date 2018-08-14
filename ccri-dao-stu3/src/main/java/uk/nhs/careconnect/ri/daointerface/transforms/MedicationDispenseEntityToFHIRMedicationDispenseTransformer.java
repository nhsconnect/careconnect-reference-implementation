package uk.nhs.careconnect.ri.daointerface.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.medicationDispense.MedicationDispenseDosage;
import uk.nhs.careconnect.ri.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.entity.medicationDispense.MedicationDispenseIdentifier;



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
        if (medicationDispenseEntity.getPatient() != null) {
            medicationDispense.setSubject(new Reference("Patient/"+medicationDispenseEntity.getPatient().getId()));
        }
        
        if (medicationDispenseEntity.getCategoryCode() != null) {
            medicationDispense.getCategory().addCoding()
                    .setDisplay(medicationDispenseEntity.getCategoryCode().getDisplay())
                    .setSystem(medicationDispenseEntity.getCategoryCode().getSystem())
                    .setCode(medicationDispenseEntity.getCategoryCode().getCode());
        }
        if (medicationDispenseEntity.getContextEpisodeOfCare() != null) {
            medicationDispense.setContext(new Reference("EpisodeOfCare/"+medicationDispenseEntity.getContextEpisodeOfCare().getId()));
        }
        if (medicationDispenseEntity.getContextEncounter() != null) {
            medicationDispense.setContext(new Reference("Encounter/"+medicationDispenseEntity.getContextEncounter().getId()));
        }

        
        if (medicationDispenseEntity.getStatus() !=null) {
            medicationDispense.setStatus(medicationDispenseEntity.getStatus());
        }

        if (medicationDispenseEntity.getMedicationCode() != null) {
            medicationDispense.setMedication(new Reference("Medication/"+medicationDispenseEntity.getId())
                    .setDisplay(medicationDispenseEntity.getMedicationCode().getDisplay()));
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
