package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Procedure;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class ProcedureDao implements ProcedureRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(ProcedureEntity procedure) {

    }

    @Override
    public Procedure read(IdType theId) {
        return null;
    }

    @Override
    public Procedure create(Procedure procedure, IdType theId, String theProcedureal) {
        return null;
    }

    @Override
    public List<Procedure> search(ReferenceParam patient, DateRangeParam date,  ReferenceParam subject) {
        return null;
    }

    @Override
    public List<ProcedureEntity> searchEntity(ReferenceParam patient,DateRangeParam date,  ReferenceParam subject) {
        return null;
    }
}
