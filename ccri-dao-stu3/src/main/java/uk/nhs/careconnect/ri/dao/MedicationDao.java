package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.MedicationRepository;
import uk.nhs.careconnect.ri.dao.transforms.MedicationEntityToFHIRMedicationTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestIdentifier;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.MedicationStatementEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

@Repository
@Transactional
public class MedicationDao implements MedicationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    MedicationEntityToFHIRMedicationTransformer medicationRequestEntityToFHIRMedicationTransformer;

    private static final Logger log = LoggerFactory.getLogger(MedicationDao.class);

    @Override
    public MedicationEntity createEntity(FhirContext ctx, Medication medication, IdType theId, String theConditional) throws OperationOutcomeException {
        MedicationEntity medicationEntity = null;

        if (medication.hasId()) medicationEntity = readEntity(ctx, medication.getIdElement());

        if (medication.hasCode()) {

            // quit if code already exists

            List<MedicationEntity> listMedication = searchEntity(ctx, new TokenParam()
                    .setSystem(medication.getCode().getCoding().get(0).getSystem())
                    .setValue(medication.getCode().getCoding().get(0).getCode()),null);
            if (listMedication.size()>0) return listMedication.get(0);
        }
        if (medicationEntity == null) medicationEntity = new MedicationEntity();

        if (medication.hasCode()) {
            CodeableConcept concept = medication.getCode();
            ConceptEntity code = conceptDao.findAddCode(concept.getCodingFirstRep());
            if (code != null) medicationEntity.setMedicationCode(code);
        }
        em.persist(medicationEntity);
        return medicationEntity;
    }

    @Override
    public Medication create(FhirContext ctx, Medication medication, IdType theId, String theConditional) throws OperationOutcomeException {

        MedicationEntity medicationEntity = createEntity(ctx, medication, theId, theConditional);
        return medicationRequestEntityToFHIRMedicationTransformer.transform(medicationEntity);

    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        // TODO This is going to MedicationRequest
        cq.select(qb.count(cq.from(MedicationEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public List<MedicationEntity> searchEntity(FhirContext ctx, TokenParam code, StringParam id) {
        List<MedicationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MedicationEntity> criteria = builder.createQuery(MedicationEntity.class);
        Root<MedicationEntity> root = criteria.from(MedicationEntity.class);

        List<Predicate> predList = new LinkedList<>();
        List<Medication> results = new ArrayList<>();


        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }

        if (code!=null) {
            log.trace("Search on MedicationRequest.medicationCode code = "+code.getValue());
            Join<MedicationEntity, ConceptEntity> joinConcept = root.join("medicationCode", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),code.getValue());
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

        TypedQuery<MedicationEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();
        return qryResults;
    }

    @Override
    public List<Medication> search(FhirContext ctx, TokenParam code, StringParam id) {
        List<MedicationEntity> results = searchEntity(ctx,code,id);
        List<Medication> res = new ArrayList<>();

        for (MedicationEntity medication :results) {
            res.add(medicationRequestEntityToFHIRMedicationTransformer.transform(medication));
        }
        return res;
    }


    @Override
    public Medication read(FhirContext ctx, IdType theId) {
        Medication medication = null;

        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationEntity medicationEntity = readEntity(ctx, theId);

        }
        return medication;
    }



    @Override
    public MedicationEntity readEntity(FhirContext ctx, IdType theId) {
        return em.find(MedicationEntity.class, Long.parseLong(theId.getIdPart()));
    }

    @Override
    public void save(FhirContext ctx, MedicationEntity resource) {

    }
}
