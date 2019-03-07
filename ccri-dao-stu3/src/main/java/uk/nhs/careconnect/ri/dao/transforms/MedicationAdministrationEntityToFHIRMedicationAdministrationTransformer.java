package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationDosage;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationIdentifier;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationNote;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestNote;


@Component
public class MedicationAdministrationEntityToFHIRMedicationAdministrationTransformer implements Transformer<MedicationAdministrationEntity, MedicationAdministration> {

   

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationAdministrationEntityToFHIRMedicationAdministrationTransformer.class);

    
    @Override
    public MedicationAdministration transform(final MedicationAdministrationEntity medicationAdministrationEntity) {
        final MedicationAdministration medicationAdministration = new MedicationAdministration();
    try {
        Meta meta = new Meta();

        if (medicationAdministrationEntity.getUpdated() != null) {
            meta.setLastUpdated(medicationAdministrationEntity.getUpdated());
        }
        else {
            if (medicationAdministrationEntity.getCreated() != null) {
                meta.setLastUpdated(medicationAdministrationEntity.getCreated());
            }
        }
        medicationAdministration.setMeta(meta);

        for(MedicationAdministrationIdentifier identifier : medicationAdministrationEntity.getIdentifiers())
        {
            Identifier ident = medicationAdministration.addIdentifier();
            if (identifier.getSystem() != null) ident.setSystem(identifier.getSystem().getUri());
            if (identifier.getValue() != null) ident.setValue(identifier.getValue());
        }

        // part of

        if (medicationAdministrationEntity.getStatus() !=null) {
            medicationAdministration.setStatus(medicationAdministrationEntity.getStatus());
        }
        if (medicationAdministrationEntity.getCategoryCode() != null) {
            medicationAdministration.getCategory().addCoding()
                    .setDisplay(medicationAdministrationEntity.getCategoryCode().getDisplay())
                    .setSystem(medicationAdministrationEntity.getCategoryCode().getSystem())
                    .setCode(medicationAdministrationEntity.getCategoryCode().getCode());
        }
        if (medicationAdministrationEntity.getMedicationEntity() != null) {
            medicationAdministration.setMedication(new Reference("Medication/"+medicationAdministrationEntity.getMedicationEntity().getId())
                    .setDisplay(medicationAdministrationEntity.getMedicationEntity().getMedicationCode().getDisplay()));
        }

        if (medicationAdministrationEntity.getPatient() != null) {
            medicationAdministration.setSubject(new Reference("Patient/"+medicationAdministrationEntity.getPatient().getId()));
        }

        if (medicationAdministrationEntity.getContextEpisodeOfCare() != null) {
            medicationAdministration.setContext(new Reference("EpisodeOfCare/"+medicationAdministrationEntity.getContextEpisodeOfCare().getId()));
        }
        if (medicationAdministrationEntity.getContextEncounter() != null) {
            medicationAdministration.setContext(new Reference("Encounter/"+medicationAdministrationEntity.getContextEncounter().getId()));
        }

        if (medicationAdministrationEntity.getPerformerPractitioner() != null) {
            if (medicationAdministration.getPerformer().size() == 0) medicationAdministration.addPerformer();
            medicationAdministration.getPerformerFirstRep().setActor(new Reference("Practitioner/"+medicationAdministrationEntity.getPerformerPractitioner().getId()).setDisplay(medicationAdministrationEntity.getPerformerPractitioner().getNames().get(0).getDisplayName()));
        }
        if (medicationAdministrationEntity.getPerformerOrganisation() != null) {
            if (medicationAdministration.getPerformer().size() == 0) medicationAdministration.addPerformer();
            medicationAdministration.getPerformerFirstRep().setOnBehalfOf(new Reference("Organization/"+medicationAdministrationEntity.getPerformerOrganisation().getId()).setDisplay(medicationAdministrationEntity.getPerformerOrganisation().getName()));
        }
        if (medicationAdministrationEntity.getPrescription() != null) {
            medicationAdministration.setPrescription(new Reference("MedicationRequest/"+medicationAdministrationEntity.getPrescription().getId()));
        }

        if (medicationAdministrationEntity.getEffectiveEnd() !=null && medicationAdministrationEntity.getEffectiveStart() != null) {
            Period period = new Period();
            period.setStart(medicationAdministrationEntity.getEffectiveStart());
            period.setEnd(medicationAdministrationEntity.getEffectiveEnd());
            medicationAdministration.setEffective(period);
        } else {
            if (medicationAdministrationEntity.getEffectiveStart() != null) {
                DateTimeType period = new DateTimeType();
                period.setValue(medicationAdministrationEntity.getEffectiveStart());
                medicationAdministration.setEffective(period);
            }
        }



        if (medicationAdministrationEntity.getNotGiven() != null) {
            medicationAdministration.setNotGiven(medicationAdministrationEntity.getNotGiven());
        }

        if (medicationAdministrationEntity.getReasonNotGivenCode() != null) {
            medicationAdministration.addReasonNotGiven().addCoding()
                    .setDisplay(medicationAdministrationEntity.getReasonNotGivenCode().getDisplay())
                    .setSystem(medicationAdministrationEntity.getReasonNotGivenCode().getSystem())
                    .setCode(medicationAdministrationEntity.getReasonNotGivenCode().getCode());
        }

        if (medicationAdministrationEntity.getReasonCode() != null) {
            medicationAdministration.addReasonCode().addCoding()
                    .setDisplay(medicationAdministrationEntity.getReasonCode().getDisplay())
                    .setSystem(medicationAdministrationEntity.getReasonCode().getSystem())
                    .setCode(medicationAdministrationEntity.getReasonCode().getCode());
        }


        for (MedicationAdministrationDosage dosageEntity : medicationAdministrationEntity.getDosages()) {
            MedicationAdministration.MedicationAdministrationDosageComponent dosage = medicationAdministration.getDosage();



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

            if (dosageEntity.getDosageText() != null) {
                dosage.setText(dosageEntity.getDosageText());
            }

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

        for (MedicationAdministrationNote note : medicationAdministrationEntity.getNotes()) {
            Annotation annotation = medicationAdministration.addNote();
            if (note.getNoteDate() != null) {
                annotation.setTime(note.getNoteDate());
            }
            if (note.getNoteText() != null) {
                annotation.setText(note.getNoteText());
            }
            if (note.getNotePatient()!=null) {
                annotation.setAuthor(new Reference("Patient/"+note.getNotePatient().getId()));
            }
            if (note.getNotePractitioner()!=null) {
                annotation.setAuthor(new Reference("Practitioner/"+note.getNotePractitioner().getId()));
            }
        }

        medicationAdministration.setId(medicationAdministrationEntity.getId().toString());
    }
    catch (Exception ex) {
        log.error(ex.getMessage());
    }
        return medicationAdministration;

    }
}
