package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.careTeam.*;

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



        for (CareTeamIdentifier identifier : careTeamEntity.getIdentifiers()) {
            careTeam.addIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
        }




        return careTeam;

    }
}
