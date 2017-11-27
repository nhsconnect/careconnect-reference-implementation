package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.medication.MedicationStatementEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class MedicationStatementDao implements MedicationStatementRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(FhirContext ctx, MedicationStatementEntity statement) {

    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationStatementEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public MedicationStatementEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }


    @Override
    public MedicationStatement read(FhirContext ctx,IdType theId) {
        return null;
    }

    @Override
    public MedicationStatement create(FhirContext ctx,MedicationStatement statement, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<MedicationStatement> search(FhirContext ctx,ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status) {
        return null;
    }

    @Override
    public List<MedicationStatementEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status) {
        return null;
    }
}
