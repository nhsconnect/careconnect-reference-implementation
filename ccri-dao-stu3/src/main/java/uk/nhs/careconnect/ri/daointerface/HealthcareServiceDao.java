package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.entity.referral.ReferralRequestEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class HealthcareServiceDao implements HealthcareServiceRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(FhirContext ctx, HealthcareServiceEntity location) {

    }

    @Override
    public HealthcareService read(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public HealthcareServiceEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public HealthcareService create(FhirContext ctx, HealthcareService location, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<HealthcareService> searchHealthcareService(FhirContext ctx, TokenParam identifier, StringParam name, TokenOrListParam codes, TokenParam id, ReferenceParam organisation) {
        return null;
    }

    @Override
    public List<HealthcareServiceEntity> searchHealthcareServiceEntity(FhirContext ctx, TokenParam identifier, StringParam name, TokenOrListParam codes, TokenParam id, ReferenceParam organisation) {
        return null;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(HealthcareServiceEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}
