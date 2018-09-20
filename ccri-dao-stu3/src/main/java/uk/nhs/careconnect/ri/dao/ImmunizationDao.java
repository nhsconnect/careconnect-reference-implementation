package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.ImmunisationEntityToFHIRImmunizationTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationIdentifier;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ImmunizationDao implements ImmunizationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    LocationRepository locationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;


    private static final Logger log = LoggerFactory.getLogger(ImmunizationDao.class);

    @Autowired
    ImmunisationEntityToFHIRImmunizationTransformer immunisationEntityToFHIRImmunizationTransformer;

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ImmunisationEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }



    @Override
    public ImmunisationEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ImmunisationEntity immunisation = (ImmunisationEntity) em.find(ImmunisationEntity.class, Long.parseLong(theId.getIdPart()));
            return immunisation;
        }
        return null;

    }


    @Override
    public void save(FhirContext ctx, ImmunisationEntity immunisation) {

    }

    @Override
    public Immunization read(FhirContext ctx,IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            ImmunisationEntity immunisation = (ImmunisationEntity) em.find(ImmunisationEntity.class, Long.parseLong(theId.getIdPart()));
            return immunisation == null
                    ? null
                    : immunisationEntityToFHIRImmunizationTransformer.transform(immunisation);
        } else {
            return null;
        }

    }

    @Override
    public Immunization create(FhirContext ctx,Immunization immunisation, IdType theId, String theConditional) throws OperationOutcomeException  {
        log.debug("Immunisation.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        ImmunisationEntity immunisationEntity = null;

        if (immunisation.hasId()) immunisationEntity = readEntity(ctx, immunisation.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/immunisation")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ImmunisationEntity> results = searchEntity(ctx, null, null,null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/immunisation"),null);
                    for (ImmunisationEntity con : results) {
                        immunisationEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (immunisationEntity == null) immunisationEntity = new ImmunisationEntity();


        PatientEntity patientEntity = null;
        if (immunisation.hasPatient()) {
            log.trace(immunisation.getPatient().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(immunisation.getPatient().getReference()));
            immunisationEntity.setPatient(patientEntity);
        }
        if (immunisation.hasStatus()) {
            immunisationEntity.setStatus(immunisation.getStatus());
        }
        if (immunisation.hasNotGiven()) {
            immunisationEntity.setNotGiven(immunisation.getNotGiven());
        }
        if (immunisation.hasPrimarySource()) {
            immunisationEntity.setPrimarySource(immunisation.getPrimarySource());
        }

        if (immunisation.hasVaccineCode()) {
            ConceptEntity code = conceptDao.findAddCode(immunisation.getVaccineCode().getCoding().get(0));
            if (code != null) { immunisationEntity.setVacinationCode(code); }
            else {
                log.info("Code: Missing System/Code = "+ immunisation.getVaccineCode().getCoding().get(0).getSystem()
                        +" code = "+immunisation.getVaccineCode().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ immunisation.getVaccineCode().getCoding().get(0).getSystem()
                        +" code = "+immunisation.getVaccineCode().getCoding().get(0).getCode());
            }
        }
        if (immunisation.hasReportOrigin()) {
            ConceptEntity code = conceptDao.findAddCode(immunisation.getReportOrigin().getCoding().get(0));
            if (code != null) { immunisationEntity.setReportOrigin(code); }
            else {
                log.info("Code: Missing Origin System/Code = "+ immunisation.getReportOrigin().getCoding().get(0).getSystem()
                        +" code = "+immunisation.getReportOrigin().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing Origin System/Code = "+ immunisation.getReportOrigin().getCoding().get(0).getSystem()
                        +" code = "+immunisation.getReportOrigin().getCoding().get(0).getCode());
            }
        }
        if (immunisation.hasDate()) {
            immunisationEntity.setAdministrationDate(immunisation.getDate());
        }
        if (immunisation.hasExpirationDate()) {
            immunisationEntity.setExpirationDate(immunisation.getExpirationDate());
        }
        if (immunisation.hasLocation()) {
                LocationEntity locationEntity = locationDao.readEntity(ctx,new IdType(immunisation.getLocation().getReference()));
                immunisationEntity.setLocation(locationEntity);
        }
        if (immunisation.hasEncounter()) {
            EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(immunisation.getEncounter().getReference()));
            immunisationEntity.setEncounter(encounterEntity);
        }


        em.persist(immunisationEntity);

        for (Identifier identifier : immunisation.getIdentifier()) {
            ImmunisationIdentifier immunisationIdentifier = null;

            for (ImmunisationIdentifier orgSearch : immunisationEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    immunisationIdentifier = orgSearch;
                    break;
                }
            }
            if (immunisationIdentifier == null)  immunisationIdentifier = new ImmunisationIdentifier();

            immunisationIdentifier.setValue(identifier.getValue());
            immunisationIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            immunisationIdentifier.setImmunisation(immunisationEntity);
            em.persist(immunisationIdentifier);
        }



        return immunisationEntityToFHIRImmunizationTransformer.transform(immunisationEntity);
    }

    @Override
    public List<Immunization> search(FhirContext ctx,ReferenceParam patient, DateRangeParam date, TokenParam status, TokenParam identifier, StringParam resid) {
        List<ImmunisationEntity> qryResults = searchEntity(ctx, patient, date, status,identifier,resid);
        List<Immunization> results = new ArrayList<>();

        for (ImmunisationEntity immunisationEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Immunization immunization = immunisationEntityToFHIRImmunizationTransformer.transform(immunisationEntity);
            results.add(immunization);
        }

        return results;
    }

    @Override
    public List<ImmunisationEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam date, TokenParam status, TokenParam identifier, StringParam resid) {
        List<ImmunisationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ImmunisationEntity> criteria = builder.createQuery(ImmunisationEntity.class);
        Root<ImmunisationEntity> root = criteria.from(ImmunisationEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Immunization> results = new ArrayList<Immunization>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ImmunisationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<ImmunisationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<ImmunisationEntity, ImmunisationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (status != null) {
            Integer immstatus = null;
            switch (status.getValue().toLowerCase()) {
                case "completed":
                    immstatus = 0;
                    break;
                case "entered-in-error":
                    immstatus = 1;
                    break;
                    
            }

            Predicate p = builder.equal(root.get("status"), immstatus);
            predList.add(p);

        }
        
        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (date !=null)
        {


            if (date.getLowerBound() != null) {

                DateParam dateParam = date.getLowerBound();


                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: /*{
                        Predicate p = builder.greaterThan(root.<Date>get("administrationDate"), parameterLower);
                        predList.add(p);

                        break;
                    }*/
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("administrationDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("administrationDate"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("administrationDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("administrationDate"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (date.getUpperBound() != null) {

                DateParam dateParam = date.getUpperBound();

                log.debug("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("administrationDate"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("administrationDate"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("administrationDate"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
                }
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
        criteria.orderBy(builder.desc(root.get("administrationDate")));

        TypedQuery<ImmunisationEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        if (date != null) {
            if (date.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, date.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (date.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, date.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}


