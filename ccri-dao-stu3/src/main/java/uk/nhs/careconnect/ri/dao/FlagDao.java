package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;

import uk.nhs.careconnect.ri.dao.transforms.FlagEntityToFHIRFlagTransformer;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;

import uk.nhs.careconnect.ri.database.entity.flag.FlagEntity;
import uk.nhs.careconnect.ri.database.entity.flag.FlagIdentifier;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class FlagDao implements FlagRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;



    @Autowired
    @Lazy
    ConceptRepository conceptDao;



    @Autowired
    private FlagEntityToFHIRFlagTransformer flagEntityToFHIRFlagTransformer;



    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(FlagDao.class);


    @Override
    public void save(FhirContext ctx, FlagEntity flag) throws OperationOutcomeException {
        em.persist(flag);
    }

    @Override
    public Flag read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            FlagEntity flagEntity = (FlagEntity) em.find(FlagEntity.class, Long.parseLong(theId.getIdPart()));

            return flagEntity == null
                    ? null
                    : flagEntityToFHIRFlagTransformer.transform(flagEntity);

        } else {
            return null;
        }
    }

    @Override
    public List<Flag> searchFlag(FhirContext ctx, TokenParam identifier, StringParam id, ReferenceParam patient) {
        List<FlagEntity> qryResults = searchFlagEntity(ctx, identifier, id,  patient);
        List<Flag> results = new ArrayList<>();

        for (FlagEntity flag : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Flag questionnaireResponse = flagEntityToFHIRFlagTransformer.transform(flag);
            results.add(questionnaireResponse);
        }

        return results;
    }

    @Override
    public List<FlagEntity> searchFlagEntity(FhirContext ctx, TokenParam identifier, StringParam resid, ReferenceParam patient) {

            List<FlagEntity> qryResults = null;

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<FlagEntity> criteria = builder.createQuery(FlagEntity.class);

            Root<FlagEntity> root = criteria.from(FlagEntity.class);

            List<Predicate> predList = new LinkedList<Predicate>();
            List<Questionnaire> results = new ArrayList<Questionnaire>();

            if (identifier !=null)
            {
                Join<FlagEntity, FlagIdentifier> join = root.join("identifiers", JoinType.LEFT);

                Predicate p = builder.equal(join.get("value"),identifier.getValue());
                predList.add(p);
                // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

            }
            if (resid != null) {
                Predicate p = builder.equal(root.get("id"),resid.getValue());
                predList.add(p);
            }

            if (patient != null) {
                if (daoutils.isNumeric(patient.getIdPart())) {
                    Join<FlagEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                    predList.add(p);
                } else {
                    Join<FlagEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
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

            qryResults = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();
            return qryResults;
        }


    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(FlagEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public FlagEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            FlagEntity flagEntity = (FlagEntity) em.find(FlagEntity.class, Long.parseLong(theId.getIdPart()));

            return flagEntity;

        } else {
            return null;
        }
    }

    @Override
    public Flag create(FhirContext ctx,Flag flag, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {

        FlagEntity flagEntity = null;
        log.debug("Called Flag Create Condition Url: " + theConditional);
        if (theId != null) {
            flagEntity = readEntity(ctx, theId);
        }

        if (flagEntity == null) {
            flagEntity = new FlagEntity();
        }

        flagEntity.setStatus(flag.getStatus());


        if (flag.hasCode()) {
            ConceptEntity code = conceptDao.findAddCode(flag.getCode().getCoding().get(0));
            if (code != null) {
                flagEntity.setCode(code);
            }
        }

        if (flag.hasCategory()) {
            ConceptEntity code = conceptDao.findAddCode(flag.getCategory().getCoding().get(0));
            if (code != null) {
                flagEntity.setCategory(code);
            }
        }

        if (flag.hasSubject()) {
            if (flag.getSubject().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(flag.getSubject().getReference()));
                flagEntity.setPatient(patientEntity);
            }
        }

        if (flag.hasEncounter()) {

            EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(flag.getEncounter().getReference()));
            flagEntity.setEncounter(encounterEntity);

        }

        if (flag.hasPeriod()) {
            if (flag.getPeriod().hasStart()) {
                flagEntity.setStartDateTime(flag.getPeriod().getStart());
            }
            if (flag.getPeriod().hasEnd()) {
                flagEntity.setEndDateTime(flag.getPeriod().getEnd());
            }
        }

        if (flag.hasAuthor()) {
            switch (flag.getAuthor().getReferenceElement().getResourceType()) {
                case "Practitioner":
                    log.trace("Practitioner DAO :" + flag.getAuthor().getReferenceElement().getResourceType());
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(flag.getAuthor().getReference()));
                    if (practitionerEntity != null) {
                        flagEntity.setAuthorPractitioner(practitionerEntity);
                    }
                    break;
                case "Organization":
                    log.trace("Organization DAO :" + flag.getAuthor().getReferenceElement().getResourceType());
                    OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(flag.getAuthor().getReference()));
                    if (organisationEntity != null) {
                        flagEntity.setAuthorOrganisation(organisationEntity);
                    }
                    break;
            }
        }

            em.persist(flagEntity);

            for (Identifier ident : flag.getIdentifier()) {

                log.debug("Flag SDS = " + ident.getValue() + " System =" + ident.getSystem());
                FlagIdentifier flagIdentifier = null;

                for (FlagIdentifier orgSearch : flagEntity.getIdentifiers()) {
                    if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                        flagIdentifier = orgSearch;
                        break;
                    }
                }
                if (flagIdentifier == null) {
                    flagIdentifier = new FlagIdentifier();
                    flagIdentifier.setFlag(flagEntity);

                }

                flagIdentifier.setValue(ident.getValue());
                flagIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
                log.debug("Flag System Code: " + flagIdentifier.getSystemUri());

                em.persist(flagIdentifier);
            }


            // log.info("Called PERSIST id="+flagEntity.getId().toString());
            flag.setId(flagEntity.getId().toString());

            return flag;
        }


}
