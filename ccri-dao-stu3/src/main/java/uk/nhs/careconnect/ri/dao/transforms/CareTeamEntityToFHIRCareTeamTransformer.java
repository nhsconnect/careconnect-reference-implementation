package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamIdentifier;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamMember;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamReason;
import uk.nhs.careconnect.ri.database.entity.careTeam.*;

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
            careTeam.addManagingOrganization(new Reference("Organization/"+careTeamEntity.getManagingOrganisation().getId()));
        }

        for (CareTeamIdentifier identifier : careTeamEntity.getIdentifiers()) {
            careTeam.addIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
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
                participant.setMember(new Reference("Organization/"+careTeamMember.getMemberOrganisation().getId()));
            }
            if (careTeamMember.getMemberPractitioner() != null) {
                participant.setMember(new Reference("Practitioner/"+careTeamMember.getMemberPractitioner().getId()));
            }
            if (careTeamMember.getMemberPerson() != null) {
                participant.setMember(new Reference("RelatedPerson/"+careTeamMember.getMemberPerson().getId()));
            }
            if (careTeamMember.getMemberPatient() != null) {
                participant.setMember(new Reference("Patient/"+careTeamMember.getMemberPatient().getId()));
            }
            if (careTeamMember.getOnBehalfOrganisation() != null) {
                participant.setOnBehalfOf(new Reference("Organization/"+careTeamMember.getOnBehalfOrganisation().getId()));
            }

        }
        for (CareTeamReason reason : careTeamEntity.getReasons()) {
            careTeam.addReasonReference(new Reference("Condition/" + reason.getCondition().getId()));
        }


        return careTeam;

    }
}
