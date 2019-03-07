package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.encounter.*;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
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

        Extension serviceType = encounter.addExtension(); 
        
        if (encounterEntity.getServiceType() != null)
        {
        	serviceType.setUrl(CareConnectExtension.UrlServiceType);
        	CodeableConcept type = new CodeableConcept();
        	type.addCoding()
	              	.setSystem(encounterEntity.getServiceType().getSystem())
	                .setCode(encounterEntity.getServiceType().getCode())
	                .setDisplay(encounterEntity.getServiceType().getDisplay());
        	serviceType.setValue(type);	
        }
        for(EncounterIdentifier identifier : encounterEntity.getIdentifiers())
        {
            Identifier ident = encounter.addIdentifier();
            if (identifier.getSystem() != null) ident.setSystem(identifier.getSystem().getUri());
            if (identifier.getValue() != null) ident.setValue(identifier.getValue());
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

        for (EncounterLocation encounterLocation : encounterEntity.getLocations()) {
            Encounter.EncounterLocationComponent encounterLocationComponent = encounter.addLocation();
            if (encounterLocation.getStatus() != null) {
                encounterLocationComponent.setStatus(encounterLocation.getStatus());
            }
            if (encounterLocation.getLocation() != null) {
                encounterLocationComponent
                        .setLocation(new Reference("Location/" + encounterLocation.getLocation().getId())
                                .setDisplay(encounterLocation.getLocation().getName()));
            }
            if (encounterLocation.getPeriodStartDate() != null || encounterLocation.getPeriodStartDate() != null) {
                if (encounterLocation.getPeriodStartDate() != null) {
                    encounterLocationComponent.getPeriod().setStart(encounterLocation.getPeriodStartDate());
                }
                if (encounterLocation.getPeriodStartDate() != null) {
                    encounterLocationComponent.getPeriod().setEnd(encounterLocation.getPeriodEndDate());
                }
            }
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
            encounter.addDiagnosis().setCondition(new Reference("Condition/"+diagnosis.getCondition().getId()).setDisplay(diagnosis.getCondition().getCode().getDisplay()));
        }

        if (encounterEntity.getPartOfEncounter() != null) {
            encounter.setPartOf(new Reference("Encounter/"+encounterEntity.getPartOfEncounter().getId()));
        }

        return encounter;

    }
}
