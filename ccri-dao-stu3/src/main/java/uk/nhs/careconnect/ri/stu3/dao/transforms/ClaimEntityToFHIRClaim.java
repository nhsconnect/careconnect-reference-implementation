package uk.nhs.careconnect.ri.stu3.dao.transforms;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Claim;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimEntity;

@Component
public class ClaimEntityToFHIRClaim implements Transformer<ClaimEntity, Claim> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareTeamEntity.class);

    @Override
    public Claim transform(ClaimEntity claimEntity) {
        return null;
    }

    public Claim transform(ClaimEntity claimEntity, FhirContext ctx) {

        final Claim claim = (Claim) ctx.newJsonParser().parseResource(claimEntity.getResource());

        claim.setId(claimEntity.getId().toString());


        return claim;
    }
}
