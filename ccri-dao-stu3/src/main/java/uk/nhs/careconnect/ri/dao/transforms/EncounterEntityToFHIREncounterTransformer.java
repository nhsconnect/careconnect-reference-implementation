package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.encounter.*;
import uk.nhs.careconnect.ri.database.entity.encounter.*;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class EncounterEntityToFHIREncounterTransformer implements Transformer<EncounterEntity, Encounter> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncounterEntityToFHIREncounterTransformer.class);


    @Override
    public Encounter transform(final EncounterEntity encounterEntity) {
        final Encounter encounter = new Encounter();

        Meta meta = new Meta().addProfile(CareConnectProfile.Encounter_1);

        if (encounterEntity.getUpdated() != null) {
            meta.setLastUpdated(encounterEntity.getUpdated());
        }
        else {
            if (encounterEntity.getCreated() != null) {
                meta.setLastUpdated(encounterEntity.getCreated());
            }
        }
        encounter.setMeta(meta);

        encounter.setId(encounterEntity.getId().toString());

        for(EncounterIdentifier identifier : encounterEntity.getIdentifiers())
        {
            encounter.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        for(EncounterEpisode episode : encounterEntity.getEpisodes())
        {
            encounter.addEpisodeOfCare()
                    .setReference("EpisodeOfCare/"+episode.getEpisode().getId());
        }

        if (encounterEntity.getPatient() != null) {
            encounter
                    .setSubject(new Reference("Patient/"+encounterEntity.getPatient().getId())
                    .setDisplay(encounterEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (encounterEntity.getServiceProvider() != null) {
            encounter
                    .setServiceProvider(new Reference("Organization/"+encounterEntity.getServiceProvider().getId())
                    .setDisplay(encounterEntity.getServiceProvider().getName()));
        }
        if (encounterEntity.getType() != null) {
            encounter.addType().addCoding()
                    .setCode(encounterEntity.getType().getCode())
                    .setSystem(encounterEntity.getType().getSystem())
                    .setDisplay(encounterEntity.getType().getDisplay());
        }
        if (encounterEntity._getClass() != null) {
            encounter.getClass_()
                    .setCode(encounterEntity._getClass().getCode())
                    .setSystem(encounterEntity._getClass().getSystem())
                    .setDisplay(encounterEntity._getClass().getDisplay());
        }
        if (encounterEntity.getStatus() !=null) {
            encounter.setStatus(encounterEntity.getStatus());
        }

        if (encounterEntity.getPeriodStartDate() != null || encounterEntity.getPeriodEndDate() != null) {
            Period period = new Period();
            if (encounterEntity.getPeriodStartDate() != null ) {
                period.setStart(encounterEntity.getPeriodStartDate());
            }
            if (encounterEntity.getPeriodEndDate() != null) {
                period.setEnd(encounterEntity.getPeriodEndDate());
            }
            encounter.setPeriod(period);
        }

        if (encounterEntity.getLocation()!=null) {
            encounter.addLocation()
                    .setLocation(new Reference("Location/"+encounterEntity.getLocation().getId())
                    .setDisplay(encounterEntity.getLocation().getName()));
        }

        for (EncounterParticipant encounterParticipant : encounterEntity.getParticipants()) {
            Encounter.EncounterParticipantComponent participantComponent = encounter.addParticipant();

            if (encounterParticipant.getParticipant() != null) {
                participantComponent
                        .setIndividual(new Reference("Practitioner/" + encounterParticipant.getParticipant().getId()))
                        .getIndividual().setDisplay(encounterParticipant.getParticipant().getNames().get(0).getDisplayName());
            }
            if (encounterParticipant.getPerson() != null) {
                participantComponent
                        .setIndividual(new Reference("RelatedPerson/" + encounterParticipant.getPerson().getId()))
                        .getIndividual().setDisplay(encounterParticipant.getPerson().getNames().get(0).getDisplayName());
            }
            if (encounterParticipant.getParticipantType() != null) {
                participantComponent.addType().addCoding()
                        .setCode(encounterParticipant.getParticipantType().getCode())
                        .setSystem(encounterParticipant.getParticipantType().getSystem())
                        .setDisplay(encounterParticipant.getParticipantType().getDisplay());
            }
        }
        if (encounterEntity.getPriority() != null) {
            encounter.getPriority()
                    .addCoding()
                        .setCode(encounterEntity.getPriority().getCode())
                        .setSystem(encounterEntity.getPriority().getSystem())
                        .setDisplay(encounterEntity.getPriority().getDisplay());
        }

        for (EncounterDiagnosis diagnosis : encounterEntity.getDiagnoses()) {
            encounter.addDiagnosis().setCondition(new Reference("Condition/"+diagnosis.getCondition().getId()));
        }

        return encounter;

    }
}
