package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.ProcedureEntityToFHIRProcedureTransformer;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ProcedureDao implements ProcedureRepository {

    @PersistenceContext
    EntityManager em;
    
    @Autowired
    ProcedureEntityToFHIRProcedureTransformer procedureEntityToFHIRProcedureTransformer;

    @Override
    public void save(ProcedureEntity procedure) {

    }

    @Override
    public Procedure read(IdType theId) {

        ProcedureEntity procedure = (ProcedureEntity) em.find(ProcedureEntity.class,Long.parseLong(theId.getIdPart()));

        return procedure == null
                ? null
                : procedureEntityToFHIRProcedureTransformer.transform(procedure);
    }

    @Override
    public Procedure create(Procedure procedure, IdType theId, String theProcedureal) {
        return null;
    }

    @Override
    public List<Procedure> search(ReferenceParam patient, DateRangeParam date,  ReferenceParam subject) {

        List<ProcedureEntity> qryResults = searchEntity(patient, date, subject);
        List<Procedure> results = new ArrayList<>();

        for (ProcedureEntity procedureEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Procedure procedure = procedureEntityToFHIRProcedureTransformer.transform(procedureEntity);
            results.add(procedure);
        }

        return results;
    }

    @Override
    public List<ProcedureEntity> searchEntity(ReferenceParam patient,DateRangeParam date,  ReferenceParam subject) {
        List<ProcedureEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ProcedureEntity> criteria = builder.createQuery(ProcedureEntity.class);
        Root<ProcedureEntity> root = criteria.from(ProcedureEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Procedure> results = new ArrayList<Procedure>();

        if (patient != null) {
            Join<ProcedureEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
            predList.add(p);
        }


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).getResultList();
        return qryResults;
    }
}
