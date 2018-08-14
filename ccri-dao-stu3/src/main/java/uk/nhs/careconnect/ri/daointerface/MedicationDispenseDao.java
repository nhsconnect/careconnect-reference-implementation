package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.transforms.MedicationDispenseEntityToFHIRMedicationDispenseTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.entity.medicationDispense.MedicationDispenseIdentifier;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static uk.nhs.careconnect.ri.daointerface.daoutils.MAXROWS;

@Repository
@Transactional
public class MedicationDispenseDao implements MedicationDispenseRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;
    
    @Autowired
    MedicationDispenseEntityToFHIRMedicationDispenseTransformer medicationDispenseEntityToFHIRMedicationDispenseTransformer;

    private static final Logger log = LoggerFactory.getLogger(MedicationDispenseDao.class);

    @Override
    public void save(FhirContext ctx, MedicationDispenseEntity statement) throws OperationOutcomeException {

    }

    @Override
    public MedicationDispense read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationDispenseEntity medicationDispense = (MedicationDispenseEntity) em.find(MedicationDispenseEntity.class, Long.parseLong(theId.getIdPart()));
            return medicationDispenseEntityToFHIRMedicationDispenseTransformer.transform(medicationDispense);
        }
        return null;
    }

    @Override
    public MedicationDispense create(FhirContext ctx, MedicationDispense statement, IdType theId, String theConditional) throws OperationOutcomeException {
        return null;
    }

    @Override
    public List<MedicationDispense> search(FhirContext ctx, ReferenceParam patient, TokenParam status, TokenParam id, TokenParam identifier, TokenParam code, ReferenceParam medication) {
        List<MedicationDispenseEntity> qryResults = searchEntity(ctx,patient, status,id,identifier,code,medication);
        List<MedicationDispense> results = new ArrayList<>();

        for (MedicationDispenseEntity medicationDispenseIntoleranceEntity : qryResults) {
            MedicationDispense medicationDispense = medicationDispenseEntityToFHIRMedicationDispenseTransformer.transform(medicationDispenseIntoleranceEntity);
            results.add(medicationDispense);
        }

        return results;
    }

    @Override
    public List<MedicationDispenseEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam status, TokenParam resid, TokenParam identifier, TokenParam code, ReferenceParam medication) {
        List<MedicationDispenseEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MedicationDispenseEntity> criteria = builder.createQuery(MedicationDispenseEntity.class);
        Root<MedicationDispenseEntity> root = criteria.from(MedicationDispenseEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<MedicationDispense> results = new ArrayList<MedicationDispense>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<MedicationDispenseEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<MedicationDispenseEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        // REVISIT KGM 28/2/2018 Added Medication search. This is using itself not Medication table

        if (medication != null) {
            Predicate p = builder.equal(root.get("id"),medication.getIdPart());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<MedicationDispenseEntity, MedicationDispenseIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (code!=null) {
            log.trace("Search on MedicationDispense.medicationCode code = "+code.getValue());
            Join<MedicationDispenseEntity, ConceptEntity> joinConcept = root.join("medicationCode", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),code.getValue());
            predList.add(p);
        }

        if (status != null) {
            Integer presstatus = null;
            switch (status.getValue().toLowerCase()) {
                case "active":
                    presstatus = 0;
                    break;
                case "on-hold":
                    presstatus = 1;
                    break;
                case "cancelled":
                    presstatus = 2;
                    break;
                case "completed":
                    presstatus = 3;
                    break;
                case "entered-in-error":
                    presstatus = 4;
                    break;
                case "stopped":
                    presstatus = 5;
                    break;
                case "draft":
                    presstatus = 6;
                    break;
                case "unknown":
                    presstatus = 7;
                    break;

            }

            Predicate p = builder.equal(root.get("status"), presstatus);
            predList.add(p);

        }


        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);



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
        criteria.orderBy(builder.desc(root.get("authoredDate")));
        TypedQuery<MedicationDispenseEntity> typedQuery = em.createQuery(criteria).setMaxResults(MAXROWS);

        qryResults = typedQuery.getResultList();
        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationDispenseEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public MedicationDispenseEntity readEntity(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationDispenseEntity medicationDispense = (MedicationDispenseEntity) em.find(MedicationDispenseEntity.class, Long.parseLong(theId.getIdPart()));
            return medicationDispense;
        }
        return null;
    }
}
