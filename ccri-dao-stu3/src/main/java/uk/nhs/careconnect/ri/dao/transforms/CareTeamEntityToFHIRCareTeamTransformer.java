package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanCategory;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamIdentifier;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamMember;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamReason;
import uk.nhs.careconnect.ri.database.entity.careTeam.*;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationNote;

@Component
public class CareTeamEntityToFHIRCareTeamTransformer implements Transformer<CareTeamEntity, CareTeam> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareTeamEntity.class);


    @Override
    public CareTeam transform(final CareTeamEntity careTeamEntity) {
        final CareTeam careTeam = new CareTeam();

        Meta meta = new Meta();

        if (careTeamEntity.getUpdated() != null) {
            meta.setLastUpdated(careTeamEntity.getUpdated());
        }
        else {
            if (careTeamEntity.getCreated() != null) {
                meta.setLastUpdated(careTeamEntity.getCreated());
            }
        }
        careTeam.setMeta(meta);

        careTeam.setId(careTeamEntity.getId().toString());

        
        if (careTeamEntity.getPatient() != null) {
            careTeam
                    .setSubject(new Reference("Patient/"+careTeamEntity.getPatient().getId())
                    .setDisplay(careTeamEntity.getPatient().getNames().get(0).getDisplayName()));
        }

        if (careTeamEntity.getStatus() != null) {
            careTeam.setStatus(careTeamEntity.getStatus());
        }
        for (CareTeamCategory categoryEntity : careTeamEntity.getCategories()) {
            CodeableConcept concept = careTeam.addCategory();
            concept.addCoding()
                    .setSystem(categoryEntity.getCategory().getSystem())
                    .setCode(categoryEntity.getCategory().getCode())
                    .setDisplay(categoryEntity.getCategory().getDisplay());
        }

        if (careTeamEntity.getName() != null) {
            careTeam.setName(careTeamEntity.getName());
        }
        

        if (careTeamEntity.getContextEncounter()!=null) {
            careTeam.setContext(new Reference("Encounter/"+careTeamEntity.getContextEncounter().getId()));
        }

        Period period = careTeam.getPeriod();
        if (careTeamEntity.getPeriodStartDateTime() != null) {
            period.setStart(careTeamEntity.getPeriodStartDateTime());
        }
        if (careTeamEntity.getPeriodEndDateTime() != null) {
            period.setEnd(careTeamEntity.getPeriodEndDateTime());
        }

        if (careTeamEntity.getReasonCode() != null) {
            careTeam.addReasonCode().addCoding()
                    .setSystem(careTeamEntity.getReasonCode().getSystem())
                    .setDisplay(careTeamEntity.getReasonCode().getDisplay())
                    .setCode(careTeamEntity.getReasonCode().getCode());
        }

        if (careTeamEntity.getManagingOrganisation() != null) {
            careTeam.addManagingOrganization(new Reference("Organization/"+careTeamEntity.getManagingOrganisation().getId()).setDisplay(careTeamEntity.getManagingOrganisation().getName()));
        }

        for (CareTeamIdentifier identifier : careTeamEntity.getIdentifiers()) {
            Identifier ident = careTeam.addIdentifier();
            ident = daoutils.getIdentifier(identifier, ident);
        }

        for (CareTeamMember careTeamMember : careTeamEntity.getMembers()) {
            CareTeam.CareTeamParticipantComponent participant = careTeam.addParticipant();

            if (careTeamMember.getRole() !=null) {
                participant.getRole().addCoding()
                        .setSystem(careTeamMember.getRole().getSystem())
                        .setDisplay(careTeamMember.getRole().getDisplay())
                        .setCode(careTeamMember.getRole().getCode());
            }
            if (careTeamMember.getMemberOrganisation() != null) {
                participant.setMember(new Reference("Organization/"+careTeamMember.getMemberOrganisation().getId()).setDisplay(careTeamMember.getMemberOrganisation().getName()));
            }
            if (careTeamMember.getMemberPractitioner() != null) {
                participant.setMember(new Reference("Practitioner/"+careTeamMember.getMemberPractitioner().getId()).setDisplay(careTeamMember.getMemberPractitioner().getNames().get(0).getDisplayName()));
            }
            if (careTeamMember.getMemberPerson() != null) {
                participant.setMember(new Reference("RelatedPerson/"+careTeamMember.getMemberPerson().getId()).setDisplay(careTeamMember.getMemberPerson().getNames().get(0).getDisplayName()));
            }
            if (careTeamMember.getMemberPatient() != null) {
                participant.setMember(new Reference("Patient/"+careTeamMember.getMemberPatient().getId()).setDisplay(careTeamMember.getMemberPatient().getNames().get(0).getDisplayName()));
            }
            if (careTeamMember.getOnBehalfOrganisation() != null) {
                participant.setOnBehalfOf(new Reference("Organization/"+careTeamMember.getOnBehalfOrganisation().getId()).setDisplay(careTeamMember.getOnBehalfOrganisation().getName()));
            }

        }
        for (CareTeamReason reason : careTeamEntity.getReasons()) {
            careTeam.addReasonReference(new Reference("Condition/" + reason.getCondition().getId()));
        }

        for (CareTeamNote note : careTeamEntity.getNotes()) {
            Annotation annotation = careTeam.addNote();
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


        return careTeam;

    }
}
