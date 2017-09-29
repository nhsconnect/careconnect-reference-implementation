package uk.nhs.careconnect.ri.dao.Dstu2.Organisation;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.dao.Stu3.CodeSystem.CodeSystemRepository;
import uk.nhs.careconnect.ri.dao.Stu3.CodeSystem.ConceptRepository;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationAddress;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationIdentifier;
import uk.nhs.careconnect.ri.entity.organization.OrganisationTelecom;
import uk.org.hl7.fhir.core.Dstu2.CareConnectSystem;

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
    private ConceptRepository codeSvc;

    @Autowired
    private OrganisationEntityToFHIROrganizationTransformer organizationEntityToFHIROrganizationTransformer;

    private static final Logger log = LoggerFactory.getLogger(RIOrganisationRepository.class);

    public void save(OrganisationEntity organization)
    {
        em.persist(organization);
    }

    public Organization read(IdType theId) {
        if (theId.getIdPart() != null) {
            OrganisationEntity organizationEntity = (OrganisationEntity) em.find(OrganisationEntity.class, Long.parseLong(theId.getIdPart()));

            return organizationEntity == null
                    ? null
                    : organizationEntityToFHIROrganizationTransformer.transform(organizationEntity);
        }
        else { return null; }

    }

    public OrganisationEntity readEntity(IdType theId) {

    if (theId.getIdPart() != null) {
        OrganisationEntity organizationEntity = (OrganisationEntity) em.find(OrganisationEntity.class, Long.parseLong(theId.getIdPart()));

        return organizationEntity;
    } else {
        return null;
    }
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
        if (theConditional != null) {
            try {

                //CareConnectSystem.ODSOrganisationCode
                if (theConditional.contains("fhir.nhs.uk/Id/ods-organization-code")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<OrganisationEntity> results = searchOrganization(new TokenParam().setValue(spiltStr[1]).setSystem(CareConnectSystem.ODSOrganisationCode));
                    for (OrganisationEntity org : results) {
                        organisationEntity = org;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }
        if (organisationEntity == null) {
            organisationEntity = new OrganisationEntity();
        }
        organisationEntity.setName(organisation.getName());
        organisationEntity.setActive(organisation.getActive());

        if (organisation.getType().getCoding().size()>0) {
            if (organisation.getType().getCoding().get(0).getSystem().equals(CareConnectSystem.OrganisationType)) {
                organisationEntity.setType(codeSvc.findCode(CareConnectSystem.OrganisationType,organisation.getType().getCoding().get(0).getCode()));
            }
            if (organisation.getType().getCoding().get(0).getSystem().equals(CareConnectSystem.SNOMEDCT)) {
                organisationEntity.setType(codeSvc.findCode(CareConnectSystem.SNOMEDCT,organisation.getType().getCoding().get(0).getCode()));
            }
        }

        if (organisation.getPartOf() != null) {
            log.debug("Ref="+organisation.getPartOf().getReference());
            organisationEntity.setPartOf(readEntity(new IdType().setValue(organisation.getPartOf().getReference())));
        }

        em.persist(organisationEntity);

        for (Identifier ident : organisation.getIdentifier()) {
            OrganisationIdentifier organisationIdentifier = null;

            for (OrganisationIdentifier orgSearch : organisationEntity.getIdentifiers()) {
                if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                    organisationIdentifier = orgSearch;
                    break;
                }
            }
            if (organisationIdentifier == null)  organisationIdentifier = new OrganisationIdentifier();

            organisationIdentifier.setValue(ident.getValue());
            organisationIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
            organisationIdentifier.setOrganisation(organisationEntity);
            em.persist(organisationIdentifier);
        }

        for (ContactPoint telecom : organisation.getTelecom()) {
            OrganisationTelecom organisationTelecom = null;

            for (OrganisationTelecom orgSearch : organisationEntity.getTelecoms()) {
                if (telecom.getValue().equals(orgSearch.getValue())) {
                    organisationTelecom = orgSearch;
                    break;
                }
            }
            if (organisationTelecom == null) {
                organisationTelecom = new OrganisationTelecom();
                organisationTelecom.setOrganizationEntity(organisationEntity);
            }

            organisationTelecom.setValue(telecom.getValue());
            organisationTelecom.setSystemDstu2(telecom.getSystem());
            organisationTelecom.setTelecomUseDstu2(telecom.getUse());

            em.persist(organisationTelecom);
        }
        for (Address address  :organisation.getAddress()) {

            OrganisationAddress organisationAddress = null;

            if (organisationEntity.getAddresses().size()>0) organisationAddress = organisationEntity.getAddresses().get(0);
            if (organisationAddress == null) {
                organisationAddress = new OrganisationAddress();
                organisationAddress.setOrganisation(organisationEntity);
            }
            AddressEntity addr = organisationAddress.getAddress();
            if (addr == null) {
                addr = organisationAddress.setAddress(new AddressEntity());
            }

            if (address.getLine().size()>0) addr.setAddress1(address.getLine().get(0).getValue());
            if (address.getLine().size()>1) addr.setAddress2(address.getLine().get(1).getValue());
            if (address.getLine().size()>2) addr.setAddress3(address.getLine().get(2).getValue());
            if (address.getCity() != null) addr.setCity(address.getCity());
            if (address.getDistrict() != null) addr.setCounty(address.getDistrict());
            if (address.getPostalCode() != null) addr.setPostcode(address.getPostalCode());
            if (address.getUse() != null) organisationAddress.setAddressUseDstu2(address.getUse());
            em.persist(addr);
            em.persist(organisationAddress);

        }

        log.debug("Called PERSIST id="+organisationEntity.getId().toString());
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
            @OptionalParam(name = Organization.SP_NAME) StringParam name
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

        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%"+name.getValue()+"%"))
                    );

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

        for (OrganisationEntity organizationEntity : qryResults)
        {
           // log.trace("HAPI Custom = "+doc.getId());
            Organization organization = organizationEntityToFHIROrganizationTransformer.transform(organizationEntity);
            results.add(organization);
        }

        return results;
    }


}
