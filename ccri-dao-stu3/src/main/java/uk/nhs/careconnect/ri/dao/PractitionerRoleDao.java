package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.PractitionerRoleToFHIRPractitionerRoleTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.*;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

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
public class PractitionerRoleDao implements PractitionerRoleRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private PractitionerRoleToFHIRPractitionerRoleTransformer
            practitionerRoleToFHIRPractitionerRoleTransformer;

    @Autowired
    private OrganisationRepository organisationDao;

    @Autowired
    private PractitionerRepository practitionerDao;

    @Autowired
    private CodeSystemRepository codeSystemDao;

    @Autowired
    private ConceptRepository codeDao;

    @Override
    public void save(FhirContext ctx, PractitionerRole practitioner) {

    }

    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole read(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            PractitionerRole roleEntity = (PractitionerRole) em.find(PractitionerRole.class, Long.parseLong(theId.getIdPart()));
            return roleEntity == null
                    ? null
                    : practitionerRoleToFHIRPractitionerRoleTransformer.transform(roleEntity);
        } else {
            return null;
        }

    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(PractitionerRole.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public PractitionerRole readEntity(FhirContext ctx, IdType theId) {
       return (PractitionerRole) em.find(PractitionerRole.class,Long.parseLong(theId.getIdPart()));
    }

    private static final Logger log = LoggerFactory.getLogger(PractitionerRoleDao.class);


    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole create(FhirContext ctx, org.hl7.fhir.dstu3.model.PractitionerRole practitionerRole, IdType theId, String theConditional) throws OperationOutcomeException  {

        PractitionerRole roleEntity = null;
        if (practitionerRole.hasId()) {
            roleEntity = (PractitionerRole) em.find(PractitionerRole.class, Long.parseLong(practitionerRole.getId()));
        }
        log.trace("theConditionalUrl = "+theConditional);
        if (theConditional != null) {
            try {
                log.trace("Contains is not null");
                //CareConnectSystem.ODSPractitionerCode
                if (theConditional.contains("fhir.nhs.uk/Id/sds-user-id")) {
                    log.trace("Contains "+theConditional);
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.trace(query);
                    String[] spiltStr = query.split("%7C");
                    log.trace(spiltStr[1]);

                    List<PractitionerRole> results = searchEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem(CareConnectSystem.SDSUserId), null, null,null);
                    for (PractitionerRole org : results) {
                        roleEntity = org;
                        break;
                    }
                } else {
                    log.trace("NOT SUPPORTED: Conditional Url = " + theConditional);
                }

            } catch (Exception ex) {
                log.error("Exception "+ex.getMessage());
            }
        }
        if (roleEntity == null) {
            roleEntity = new PractitionerRole();
        }
        if (practitionerRole.getPractitioner() != null) {
            roleEntity.setPractitioner(practitionerDao.readEntity(ctx, new IdType(practitionerRole.getPractitioner().getReference())));
        }

        if (practitionerRole.getOrganization() != null) {
            roleEntity.setOrganisation(organisationDao.readEntity(ctx, new IdType(practitionerRole.getOrganization().getReference())));
        }

        if (practitionerRole.getCode().size() > 0) {
            if (practitionerRole.getCode().get(0).getCoding().get(0).getSystem().equals(CareConnectSystem.SDSJobRoleName)) {
                roleEntity.setRole(codeDao.findCode(practitionerRole.getCode().get(0).getCoding().get(0)));
            }
        }
        em.persist(roleEntity);

        for (CodeableConcept specialty : practitionerRole.getSpecialty()) {
            Boolean found = false;
            ConceptEntity specialtyConcept = codeDao.findCode(specialty.getCoding().get(0));
            for (PractitionerSpecialty searchSpecialty : roleEntity.getSpecialties()) {
                log.trace("Already has specialty = " + searchSpecialty.getSpecialty().getCode() + " code "+searchSpecialty.getSpecialty().getSystem());
                if (searchSpecialty.getSpecialty().getCode().equals(specialtyConcept.getCode())
                        && searchSpecialty.getSpecialty().getSystem().equals(specialtyConcept.getSystem())) found = true;
            }
            try {
                if (!found){
                    log.trace("not found! specialty = " + specialty.getCoding().get(0).getCode() + " code "+specialty.getCoding().get(0).getSystem());
                    PractitionerSpecialty practitionerSpecialty = new PractitionerSpecialty();
                    practitionerSpecialty.setPractitionerRole(roleEntity);
                    practitionerSpecialty.setSpecialty(specialtyConcept);
                    em.persist(practitionerSpecialty);
                    roleEntity.getSpecialties().add(practitionerSpecialty);
                }
            } catch (Exception ex) {

            }
        }

        for (PractitionerRoleIdentifier orgSearch : roleEntity.getIdentifiers()) {
            em.remove(orgSearch);
        }

        for (Identifier identifier : practitionerRole.getIdentifier()) {
            log.trace("Recieved identifier = " + identifier.getSystem() + " code "+identifier.getValue());

            PractitionerRoleIdentifier practitionerRoleIdentifier = null;
/*
            for (PractitionerRoleIdentifier orgSearch : roleEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    practitionerRoleIdentifier = orgSearch;
                    break;
                }
            }
            */
            if (practitionerRoleIdentifier == null) {
                practitionerRoleIdentifier = new PractitionerRoleIdentifier();

                log.trace("Not found Identifier!");

                practitionerRoleIdentifier.setValue(identifier.getValue());
                practitionerRoleIdentifier.setPractitionerRole(roleEntity);
                practitionerRoleIdentifier.setSystem(codeSystemDao.findSystem(identifier.getSystem()));
                em.persist(practitionerRoleIdentifier);
                roleEntity.getIdentifiers().add(practitionerRoleIdentifier);
            }
        }
        return practitionerRole; //roleEntity;
    }

    @Override
    public List<PractitionerRole> searchEntity(FhirContext ctx,
            TokenParam identifier
            , ReferenceParam practitioner
            , ReferenceParam organisation
            , StringParam resid) {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<PractitionerRole> criteria = builder.createQuery(PractitionerRole.class);
        Root<PractitionerRole> root = criteria.from(PractitionerRole.class);

        List<Predicate> predList = new LinkedList<Predicate>();

        if (identifier !=null)
        {
            log.trace("Search on value = "+identifier.getValue());

            Join<PractitionerRole, PractitionerRoleIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (practitioner != null) {
            Join<PractitionerRole, PractitionerEntity> joinPractitioner = root.join("practitionerEntity",JoinType.LEFT);
            Predicate p = null;
            if (daoutils.isNumeric(practitioner.getIdPart())) {
                p = builder.equal(joinPractitioner.get("id"),practitioner.getIdPart());
            } else {
                p = builder.equal(joinPractitioner.get("id"),-1);
            }

            predList.add(p);
        }
        if (organisation != null) {
            Join<PractitionerRole, OrganisationEntity> joinOrganisation = root.join("managingOrganisation",JoinType.LEFT);
            Predicate p = null;
            if (daoutils.isNumeric(organisation.getIdPart())) {
                p = builder.equal(joinOrganisation.get("id"), organisation.getIdPart());
            } else {
                p = builder.equal(joinOrganisation.get("id"), -1);
            }
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

        return em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();

    }


    @Override
    public List<org.hl7.fhir.dstu3.model.PractitionerRole> search(FhirContext ctx,
            TokenParam identifier
            , ReferenceParam practitioner
            , ReferenceParam organisation
            , StringParam resid) {
        List<org.hl7.fhir.dstu3.model.PractitionerRole> results = new ArrayList<>();
        List<PractitionerRole> roles = searchEntity(ctx, identifier,practitioner,organisation,resid);
        for (PractitionerRole role : roles) {
             results.add(practitionerRoleToFHIRPractitionerRoleTransformer.transform(role));
        }
        return results;
    }

}
