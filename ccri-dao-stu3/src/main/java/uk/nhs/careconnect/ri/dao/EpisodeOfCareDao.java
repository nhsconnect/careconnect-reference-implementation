package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.EpisodeOfCareEntityToFHIREpisodeOfCareTransformer;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.SystemEntity;

import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareIdentifier;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;

import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class EpisodeOfCareDao implements EpisodeOfCareRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    private LibDao libDao;

    @Autowired
    private EpisodeOfCareEntityToFHIREpisodeOfCareTransformer episodeOfCareEntityToFHIREpisodeOfCareTransformer;

    private static final Logger log = LoggerFactory.getLogger(EpisodeOfCareDao.class);


    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(EpisodeOfCareEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public EpisodeOfCareEntity readEntity(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            EpisodeOfCareEntity episodeOfCareEntity = (EpisodeOfCareEntity) em.find(EpisodeOfCareEntity.class, Long.parseLong(theId.getIdPart()));

            return episodeOfCareEntity;
        } else {
            return null;
        }
    }

    @Override
    public void save(FhirContext ctx, EpisodeOfCareEntity resource) {

    }


    @Override
    public void save(FhirContext ctx, EpisodeOfCare episode) {

    }
    @Override
    public EpisodeOfCare read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            EpisodeOfCareEntity episode = (EpisodeOfCareEntity) em.find(EpisodeOfCareEntity.class, Long.parseLong(theId.getIdPart()));

            return episode == null
                    ? null
                    : episodeOfCareEntityToFHIREpisodeOfCareTransformer.transform(episode);
        } else {
            return null;
        }

    }

    @Override
    public EpisodeOfCare create(FhirContext ctx,EpisodeOfCare episode, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("EpisodeOfCare.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(episode));
        EpisodeOfCareEntity episodeEntity = null;

        if (theId !=null  && daoutils.isNumeric(theId.getIdPart()) ) episodeEntity = em.find(EpisodeOfCareEntity.class, Long.parseLong(theId.getIdPart()));

        if (theConditional != null) {
            try {

                //CareConnectSystem.ODSOrganisationCode
                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/episode")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<EpisodeOfCareEntity> results = searchEntity(ctx, null, null,null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/episode"));
                    for (EpisodeOfCareEntity enc : results) {
                        episodeEntity = enc;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (episodeEntity == null) episodeEntity = new EpisodeOfCareEntity();


        PatientEntity patientEntity = null;
        if (episode.hasPatient()) {
            log.trace(episode.getPatient().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(episode.getPatient().getReference()));
            episodeEntity.setPatient(patientEntity);
        }
       
        if (episode.hasType()) {
            ConceptEntity code = conceptDao.findAddCode(episode.getType().get(0).getCoding().get(0));
            if (code != null) { episodeEntity.setType(code); }
            else {
                log.info("Code: Missing System/Code = "+episode.getType().get(0).getCoding().get(0).getSystem() +" code = "+episode.getType().get(0).getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ episode.getType().get(0).getCoding().get(0).getSystem() +" code = "+episode.getType().get(0).getCoding().get(0).getCode());
            }
        }

        if (episode.hasStatus()) {
            episodeEntity.setStatus(episode.getStatus());
        }

        if (episode.hasManagingOrganization()) {
            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(episode.getManagingOrganization().getReference()));
            if (organisationEntity != null) episodeEntity.setManagingOrganisation(organisationEntity);
        }


        if (episode.hasPeriod()) {

            if (episode.getPeriod().hasStart()) {
                episodeEntity.setPeriodStartDate(episode.getPeriod().getStart());

                if (episode.getPeriod().hasEnd()) {
                    if (episode.getPeriod().getEnd().after(episode.getPeriod().getStart()))
                        episodeEntity.setPeriodEndDate(episode.getPeriod().getEnd());
                    else
                        episodeEntity.setPeriodEndDate(null); // KGM 15/12/2017 Ensure end date is after start date , if not set end date to null
                }
            }
        }

        if (episode.hasCareManager()) {
            PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(episode.getCareManager().getReference()));
            if (practitionerEntity != null) episodeEntity.setCareManager(practitionerEntity);
        }

        em.persist(episodeEntity);

        for (Identifier identifier : episode.getIdentifier()) {
            EpisodeOfCareIdentifier episodeIdentifier = null;

            for (EpisodeOfCareIdentifier orgSearch : episodeEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    episodeIdentifier = orgSearch;
                    break;
                }
            }
            if (episodeIdentifier == null)  episodeIdentifier = new EpisodeOfCareIdentifier();

            episodeIdentifier.setValue(identifier.getValue());
            episodeIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            episodeIdentifier.setEpisode(episodeEntity);
            em.persist(episodeIdentifier);
        }
        
        return episodeOfCareEntityToFHIREpisodeOfCareTransformer.transform(episodeEntity);
    }

    @Override
    public List<EpisodeOfCare> search(FhirContext ctx,ReferenceParam patient, DateRangeParam date, StringParam resid, TokenParam identifier) {

        List<EpisodeOfCareEntity> qryResults = searchEntity(ctx,patient, date,resid,identifier);
        List<EpisodeOfCare> results = new ArrayList<>();

        for (EpisodeOfCareEntity episodeEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            EpisodeOfCare episode = episodeOfCareEntityToFHIREpisodeOfCareTransformer.transform(episodeEntity);
            results.add(episode);
        }

        return results;
    }
    @Override
    public List<EpisodeOfCareEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam date, StringParam resid, TokenParam identifier) {
        List<EpisodeOfCareEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<EpisodeOfCareEntity> criteria = builder.createQuery(EpisodeOfCareEntity.class);
        Root<EpisodeOfCareEntity> root = criteria.from(EpisodeOfCareEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<EpisodeOfCare> results = new ArrayList<EpisodeOfCare>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<EpisodeOfCareEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<EpisodeOfCareEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<EpisodeOfCareEntity, EpisodeOfCareIdentifier> join = root.join("identifiers", JoinType.LEFT);
            Join<EpisodeOfCareIdentifier, SystemEntity> joinSystem = join.join("systemEntity",JoinType.LEFT);

            Predicate pvalue = builder.like(
                    builder.upper(join.get("value")),
                    builder.upper(builder.literal(daoutils.removeSpace(identifier.getValue())))
            );
            if (identifier.getSystem() != null) {
                Predicate psystem = builder.like(
                        builder.upper(joinSystem.get("codeSystemUri")),
                        builder.upper(builder.literal(identifier.getSystem()))
                );
                Predicate p = builder.and(pvalue, psystem);
                predList.add(p);
            } else {
                predList.add(pvalue);
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
}
