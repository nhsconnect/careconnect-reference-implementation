package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.allergy.AllergyIntoleranceEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class AllergyIntoleranceDao implements AllergyIntoleranceRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(AllergyIntoleranceEntity allergy) {

    }

    @Override
    public AllergyIntolerance read(IdType theId) {
        return null;
    }

    @Override
    public AllergyIntolerance create(AllergyIntolerance allergy, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<AllergyIntolerance> search(ReferenceParam patient, DateRangeParam date, TokenParam clinicalStatus) {
        return null;
    }

    @Override
    public List<AllergyIntoleranceEntity> searchEntity(ReferenceParam patient, DateRangeParam date, TokenParam clinicalStatus) {
        return null;
    }
}
