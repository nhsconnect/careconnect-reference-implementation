package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestDosage;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestIdentifier;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;


@Component
public class MedicationRequestEntityToFHIRMedicationRequestTransformer implements Transformer<MedicationRequestEntity, MedicationRequest> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationRequestEntityToFHIRMedicationRequestTransformer.class);


    public MedicationRequestEntityToFHIRMedicationRequestTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public MedicationRequest transform(final MedicationRequestEntity medicationRequestEntity) {
        final MedicationRequest medicationRequest = new MedicationRequest();
    try {
        Meta meta = new Meta().addProfile(CareConnectProfile.MedicationRequest_1);

        if (medicationRequestEntity.getUpdated() != null) {
            meta.setLastUpdated(medicationRequestEntity.getUpdated());
        }
        else {
            if (medicationRequestEntity.getCreated() != null) {
                meta.setLastUpdated(medicationRequestEntity.getCreated());
            }
        }
        medicationRequest.setMeta(meta);

        for(MedicationRequestIdentifier identifier : medicationRequestEntity.getIdentifiers())
        {
            medicationRequest.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }
        if (medicationRequestEntity.getPatient() != null) {
            medicationRequest.setSubject(new Reference("Patient/"+medicationRequestEntity.getPatient().getId()));
        }
        if (medicationRequestEntity.getSubstitutionAllowed() != null) {
                medicationRequest.getSubstitution().setAllowed(medicationRequestEntity.getSubstitutionAllowed());
        }
        if (medicationRequestEntity.getAuthoredDate() != null) {
            medicationRequest.setAuthoredOn(medicationRequestEntity.getAuthoredDate());
        }
        if (medicationRequestEntity.getCategoryCode() != null) {
            medicationRequest.getCategory().addCoding()
                    .setDisplay(medicationRequestEntity.getCategoryCode().getDisplay())
                    .setSystem(medicationRequestEntity.getCategoryCode().getSystem())
                    .setCode(medicationRequestEntity.getCategoryCode().getCode());
        }
        if (medicationRequestEntity.getContextEpisodeOfCare() != null) {
            medicationRequest.setContext(new Reference("EpisodeOfCare/"+medicationRequestEntity.getContextEpisodeOfCare().getId()));
        }
        if (medicationRequestEntity.getContextEncounter() != null) {
            medicationRequest.setContext(new Reference("Encounter/"+medicationRequestEntity.getContextEncounter().getId()));
        }

        if (medicationRequestEntity.getIntent()!= null) {
            medicationRequest.setIntent(medicationRequestEntity.getIntent());
        }
        if (medicationRequestEntity.getStatus() !=null) {
            medicationRequest.setStatus(medicationRequestEntity.getStatus());
        }

        if (medicationRequestEntity.getMedicationEntity() != null) {
            medicationRequest.setMedication(new Reference("Medication/"+medicationRequestEntity.getMedicationEntity().getId())
                    .setDisplay(medicationRequestEntity.getMedicationEntity().getMedicationCode().getDisplay()));
        }

        if (medicationRequestEntity.getPriority() != null) {
            medicationRequest.setPriority(medicationRequestEntity.getPriority());
        }
        if (medicationRequestEntity.getReasonCode() != null) {
            medicationRequest.addReasonCode().addCoding()
                    .setDisplay(medicationRequestEntity.getReasonCode().getDisplay())
                    .setSystem(medicationRequestEntity.getReasonCode().getSystem())
                    .setCode(medicationRequestEntity.getReasonCode().getCode());
        }
        if (medicationRequestEntity.getReasonCondition() != null) {
            medicationRequest.addReasonReference(new Reference(("Condition/"+medicationRequestEntity.getReasonCondition().getId())));
        }

        if (medicationRequestEntity.getReasonObservation() != null) {
            medicationRequest.addReasonReference(new Reference(("Observation/"+medicationRequestEntity.getReasonObservation().getId())));
        }
        if (medicationRequestEntity.getRecorderPractitioner() != null) {
            medicationRequest.setRecorder(new Reference("Practitioner/"+medicationRequestEntity.getRecorderPractitioner().getId())
                    .setDisplay(medicationRequestEntity.getRecorderPractitioner().getNames().get(0).getDisplayName()));
        }
        if (medicationRequestEntity.getRequesterPatient() != null) {
            MedicationRequest.MedicationRequestRequesterComponent requestor = medicationRequest.getRequester();
            requestor.setAgent(new Reference("Patient/"+medicationRequestEntity.getRequesterPatient().getId())
                    .setDisplay(medicationRequestEntity.getRequesterPatient().getNames().get(0).getDisplayName()));
        }
        if (medicationRequestEntity.getRequesterOrganisation() != null) {
            MedicationRequest.MedicationRequestRequesterComponent requestor = medicationRequest.getRequester();
            requestor.setAgent(new Reference("Organization/"+medicationRequestEntity.getRequesterOrganisation().getId())
                    .setDisplay(medicationRequestEntity.getRequesterOrganisation().getName()));
        }
        if (medicationRequestEntity.getRequesterPractitioner() != null) {
            MedicationRequest.MedicationRequestRequesterComponent requestor = medicationRequest.getRequester();
            requestor.setAgent(new Reference("Practitioner/"+medicationRequestEntity.getRequesterPractitioner().getId())
                    .setDisplay(medicationRequestEntity.getRequesterPractitioner().getNames().get(0).getDisplayName()));
        }
        if (medicationRequestEntity.getRequesterOnBehalfOfOrganisation() != null) {
            MedicationRequest.MedicationRequestRequesterComponent requestor = medicationRequest.getRequester();
            requestor.setOnBehalfOf(new Reference("Organization/"+medicationRequestEntity.getRequesterOnBehalfOfOrganisation().getId())
                    .setDisplay(medicationRequestEntity.getRequesterOnBehalfOfOrganisation().getName()));
        }

        // Default to NHS Prescription - this is a mandatory field.
        if (medicationRequestEntity.getSupplyTypeCode() != null) {
            CodeableConcept concept = new CodeableConcept();
            concept.addCoding()
                    .setDisplay(medicationRequestEntity.getSupplyTypeCode().getDisplay())
                    .setSystem(medicationRequestEntity.getSupplyTypeCode().getSystem())
                    .setCode(medicationRequestEntity.getSupplyTypeCode().getCode());
            if (medicationRequestEntity.getSupplyTypeCode().getCode().contains("39482")) {
                // NHS coding
                medicationRequest.addExtension().setUrl(CareConnectExtension.UrlMedicationSupplyType).setValue(concept);
            } else {
                // else assume (US) shared record coding
                medicationRequest.addExtension().setUrl(CareConnectExtension.ShrActionCodeExtension).setValue(concept);
            }
        } else {
            CodeableConcept concept = new CodeableConcept();
            concept.addCoding()
                    .setDisplay("NHS Prescription")
                    .setSystem(CareConnectSystem.SNOMEDCT)
                    .setCode("930811000000109");
            medicationRequest.addExtension().setUrl(CareConnectExtension.UrlMedicationSupplyType).setValue(concept);
        }


        MedicationRequest.MedicationRequestDispenseRequestComponent dispense = medicationRequest.getDispenseRequest();
        Period period = new Period();
        dispense.setValidityPeriod(period);

        if (medicationRequestEntity.getDispenseRequestEnd() !=null) {
            period.setEnd(medicationRequestEntity.getDispenseRequestEnd());
        }
        if (medicationRequestEntity.getDispenseRequestStart() !=null) {
            // KGM 15/12/2017 Corrected to setup Start date (was showing end)
            period.setStart(medicationRequestEntity.getDispenseRequestStart());
        }
        if (medicationRequestEntity.getNumberOfRepeatsAllowed() != null && medicationRequestEntity.getNumberOfRepeatsAllowed()>0) {
            dispense.setNumberOfRepeatsAllowed(medicationRequestEntity.getNumberOfRepeatsAllowed());
        }
        Duration duration = new Duration();
        if (medicationRequestEntity.getExpectedSupplyDuration() != null) {
            duration.setValue(medicationRequestEntity.getExpectedSupplyDuration());
            if (medicationRequestEntity.getDurationUnitsCode()!=null) {
                duration.setCode(medicationRequestEntity.getDurationUnitsCode().getCode())
                        .setSystem(medicationRequestEntity.getDurationUnitsCode().getSystem())
                        .setUnit(medicationRequestEntity.getDurationUnitsCode().getDisplay());
            }
            dispense.setExpectedSupplyDuration(duration);
        }

        for (MedicationRequestDosage dosageEntity : medicationRequestEntity.getDosages()) {
            Dosage dosage = medicationRequest.addDosageInstruction();
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

        medicationRequest.setId(medicationRequestEntity.getId().toString());
    }
    catch (Exception ex) {
        log.error(ex.getMessage());
    }
        return medicationRequest;

    }
}
