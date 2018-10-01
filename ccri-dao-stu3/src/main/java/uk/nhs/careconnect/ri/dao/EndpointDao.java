package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.EndpointRepository;
import uk.nhs.careconnect.ri.database.daointerface.OrganisationRepository;
import uk.nhs.careconnect.ri.dao.transforms.EndpointEntityToFHIREndpointTransform;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointEntity;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class EndpointDao implements EndpointRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    EndpointEntityToFHIREndpointTransform endpointEntityToFHIREndpointTransform;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(EndpointDao.class);
    
    @Override
    public void save(FhirContext ctx, EndpointEntity endpoint) throws OperationOutcomeException {
        
    }

    @Override
    public Endpoint read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            EndpointEntity endpoint = (EndpointEntity) em.find(EndpointEntity.class, Long.parseLong(theId.getIdPart()));

            return endpoint == null
                    ? null
                    : endpointEntityToFHIREndpointTransform.transform(endpoint);
        } else  {
            return null;
        }
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(EndpointEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public EndpointEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            EndpointEntity endpoint = (EndpointEntity) em.find(EndpointEntity.class, Long.parseLong(theId.getIdPart()));

            return endpoint;
        } else {
            return null;
        }
    }

    @Override
    public Endpoint create(FhirContext ctx, Endpoint endpoint, IdType theId, String theConditiontal) throws OperationOutcomeException {
        log.debug("Endpoint.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        EndpointEntity endpointEntity = null;

        if (endpoint.hasId()) endpointEntity = readEntity(ctx, endpoint.getIdElement());

        if (theConditiontal != null) {
            try {


                if (theConditiontal.contains("fhir.leedsth.nhs.uk/Id/clientId")) {
                    URI uri = new URI(theConditiontal);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<EndpointEntity> results = searchEndpointEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/clientId"),null);
                    for (EndpointEntity con : results) {
                        endpointEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Endpoint Url = "+theConditiontal);
                }

            } catch (Exception ex) {

            }
        }

        if (endpointEntity == null) {
            endpointEntity = new EndpointEntity();
        }

        if (endpoint.hasStatus()) {
            endpointEntity.setStatus(endpoint.getStatus());
        }

        if (endpoint.hasConnectionType()) {
            ConceptEntity code = conceptDao.findAddCode(endpoint.getConnectionType());
            if (code != null) { endpointEntity.setConnectionType(code); }
            else {
                log.info("Code: Missing System/Code = "+ endpoint.getConnectionType().getSystem() +" code = "+endpoint.getConnectionType().getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ endpoint.getConnectionType().getSystem()
                        +" code = "+endpoint.getConnectionType().getCode());
            }
        }
        if (endpoint.getManagingOrganization() != null) {
            log.debug("Endpoint Org Ref="+endpoint.getManagingOrganization().getReference());
            endpointEntity.setManagingOrganisation(organisationRepository.readEntity(ctx, new IdType().setValue(endpoint.getManagingOrganization().getReference())));
        }

        if (endpoint.hasAddress()) {
            endpointEntity.setAddress(endpoint.getAddress());
        }
        if (endpoint.hasName()) {
            endpointEntity.setName(endpoint.getName());
        }


        em.persist(endpointEntity);
        log.debug("persist endpoint");

        for (Identifier identifier : endpoint.getIdentifier()) {
            EndpointIdentifier endpointIdentifier = null;

            for (EndpointIdentifier orgSearch : endpointEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    endpointIdentifier = orgSearch;
                    break;
                }
            }
            if (endpointIdentifier == null)  endpointIdentifier = new EndpointIdentifier();

            endpointIdentifier.setValue(identifier.getValue());
            endpointIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            endpointIdentifier.setEndpoint(endpointEntity);
            em.persist(endpointIdentifier);
        }
        log.debug("persist endpoint identifiers");

        return endpointEntityToFHIREndpointTransform.transform(endpointEntity);
    }

    @Override
    public List<Endpoint> searchEndpoint(FhirContext ctx, TokenParam identifier, StringParam resid) {
        List<EndpointEntity> qryResults = searchEndpointEntity(ctx,identifier,resid);
        List<Endpoint> results = new ArrayList<>();

        for (EndpointEntity endpointEntity : qryResults)
        {
            Endpoint endpoint = endpointEntityToFHIREndpointTransform.transform(endpointEntity);
            results.add(endpoint);
        }

        return results;
    }

    @Override
    public List<EndpointEntity> searchEndpointEntity(FhirContext ctx, TokenParam identifier, StringParam resid) {
        List<EndpointEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<EndpointEntity> criteria = builder.createQuery(EndpointEntity.class);
        Root<EndpointEntity> root = criteria.from(EndpointEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Endpoint> results = new ArrayList<Endpoint>();
        
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<EndpointEntity, EndpointIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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

        //criteria.orderBy(builder.desc(root.get("assertedDateTime")));

        TypedQuery<EndpointEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);
        
        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}
