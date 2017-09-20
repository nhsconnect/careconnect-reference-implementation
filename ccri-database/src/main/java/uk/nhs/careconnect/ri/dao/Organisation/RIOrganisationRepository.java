package uk.nhs.careconnect.ri.dao.Organisation;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.dao.CodeSystem.CodeSystemRepository;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationIdentifier;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RIOrganisationRepository implements OrganisationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private OrganisationEntityToFHIROrganizationTransformer organizationEntityToFHIROrganizationTransformer;

    private static final Logger log = LoggerFactory.getLogger(RIOrganisationRepository.class);

    public void save(OrganisationEntity organization)
    {
        em.persist(organization);
    }

    public Organization read(IdType theId) {

        OrganisationEntity organizationEntity = (OrganisationEntity) em.find(OrganisationEntity.class,Long.parseLong(theId.getIdPart()));

        return organizationEntity == null
                ? null
                : organizationEntityToFHIROrganizationTransformer.transform(organizationEntity);

    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception ex) {
            return "";
        }
    }

    @Override
    public Organization create(Organization organisation, @IdParam IdType theId, @ConditionalUrlParam String theConditional) {


        OrganisationEntity organisationEntity = null;

        if (organisation.hasId()) {
            organisationEntity =  (OrganisationEntity) em.find(OrganisationEntity.class,Long.parseLong(organisation.getId()));
        }

        try {

            if (theConditional != null && theConditional.contains(CareConnectSystem.ODSOrganisationCode)) {
                URI uri = new URI(theConditional);

                String scheme = uri.getScheme();
                String host = uri.getHost();
                String query = uri.getRawQuery();
                log.info(query);
                String[] spiltStr = query.split("%7C");
                log.info(spiltStr[1]);

                List<OrganisationEntity> results = searchOrganization(new TokenParam().setValue(spiltStr[1]).setSystem(CareConnectSystem.ODSOrganisationCode));
                for (OrganisationEntity org : results) {
                    organisationEntity = org;
                    break;
                }
            }
        }
         catch (Exception ex) {

            }
        if (organisationEntity == null) {
            organisationEntity = new OrganisationEntity();
        }
        organisationEntity.setName(organisation.getName());
        em.persist(organisationEntity);

        for (Identifier ident : organisation.getIdentifier()) {
            OrganisationIdentifier organisationIdentifier = new OrganisationIdentifier();
            organisationIdentifier.setValue(ident.getValue());
            organisationIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
            organisationIdentifier.setOrganisation(organisationEntity);
            em.persist(organisationIdentifier);
        }


        log.info("Called PERSIST id="+organisationEntity.getId().toString());
        organisation.setId(organisationEntity.getId().toString());

        return organisation;
    }


    public List<OrganisationEntity> searchOrganization (
            @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier
    )
    {
        List<OrganisationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<OrganisationEntity> criteria = builder.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> root = criteria.from(OrganisationEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<OrganisationEntity> results = new ArrayList<OrganisationEntity>();

        if (identifier !=null)
        {
            Join<OrganisationEntity, OrganisationIdentifier> join = root.join("identifiers", JoinType.LEFT);

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

        qryResults = em.createQuery(criteria).getResultList();

        for (OrganisationEntity organizationEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());

            results.add(organizationEntity);
        }

        return results;
    }

    public List<Organization> searchOrganization (
            @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    )
    {
        List<OrganisationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<OrganisationEntity> criteria = builder.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> root = criteria.from(OrganisationEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Organization> results = new ArrayList<Organization>();

        if (identifier !=null)
        {
            Join<OrganisationEntity, OrganisationIdentifier> join = root.join("identifiers", JoinType.LEFT);

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

        qryResults = em.createQuery(criteria).getResultList();

        for (OrganisationEntity organizationEntity : qryResults)
        {
           // log.trace("HAPI Custom = "+doc.getId());
            Organization organization = organizationEntityToFHIROrganizationTransformer.transform(organizationEntity);
            results.add(organization);
        }

        return results;
    }


}
