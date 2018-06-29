package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.referral.ReferralRequestEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class ReferralRequestDao implements ReferralRequestRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<ReferralRequestEntity> searchReferralRequestEntity(FhirContext ctx, TokenParam identifier, TokenOrListParam codes, TokenParam id) {
        return null;
    }

    @Override
    public void save(FhirContext ctx, ReferralRequestEntity location) {

    }

    @Override
    public ReferralRequest read(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public ReferralRequestEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public ReferralRequest create(FhirContext ctx, ReferralRequest location, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<ReferralRequest> searchReferralRequest(FhirContext ctx, TokenParam identifier, TokenOrListParam codes, TokenParam id) {
        return null;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ReferralRequestEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}
