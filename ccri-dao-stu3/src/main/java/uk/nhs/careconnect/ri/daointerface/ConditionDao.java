package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class ConditionDao implements ConditionRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(ConditionEntity condition) {

    }

    @Override
    public Condition read(IdType theId) {
        return null;
    }

    @Override
    public Condition create(Condition condition, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<Condition> search(ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted) {
        return null;
    }

    @Override
    public List<ConditionEntity> searchEntity(ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted) {
        return null;
    }
}
