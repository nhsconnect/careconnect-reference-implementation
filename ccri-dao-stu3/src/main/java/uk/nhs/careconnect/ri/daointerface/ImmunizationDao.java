package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Immunization;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.immunisation.ImmunisationEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class ImmunizationDao implements ImmunizationRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(ImmunisationEntity immunisation) {

    }

    @Override
    public Immunization read(IdType theId) {
        return null;
    }

    @Override
    public Immunization create(Immunization immunisation, IdType theId, String theImmunizational) {
        return null;
    }

    @Override
    public List<Immunization> search(ReferenceParam patient, DateRangeParam date, TokenParam status) {
        return null;
    }

    @Override
    public List<ImmunisationEntity> searchEntity(ReferenceParam patient, DateRangeParam date, TokenParam status) {
        return null;
    }
}


