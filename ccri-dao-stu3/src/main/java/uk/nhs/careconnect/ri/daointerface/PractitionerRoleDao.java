package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerRole;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerRoleIdentifier;
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
    private OrganisationRepository organisationDao;

    @Autowired
    private PractitionerRepository practitionerDao;

    @Autowired
    private CodeSystemRepository codeSystemDao;

    @Autowired
    private ConceptRepository codeDao;

    @Override
    public void save(PractitionerRole practitioner) {

    }

    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole read(IdType theId) {
        return null;
    }

    @Override
    public PractitionerRole readEntity(IdType theId) {
        return null;
    }

    private static final Logger log = LoggerFactory.getLogger(PractitionerRoleDao.class);


    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole create(org.hl7.fhir.dstu3.model.PractitionerRole practitionerRole, IdType theId, String theConditional) {

        PractitionerRole roleEntity = null;
        if (practitionerRole.hasId()) {
            roleEntity = (PractitionerRole) em.find(PractitionerRole.class, Long.parseLong(practitionerRole.getId()));
        }

        if (theConditional != null) {
            try {

                //CareConnectSystem.ODSPractitionerCode
                if (theConditional.contains("fhir.nhs.uk/Id/sds-user-id")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<PractitionerRole> results = searchEntity(new TokenParam().setValue(spiltStr[1]).setSystem(CareConnectSystem.SDSUserId), null, null);
                    for (PractitionerRole org : results) {
                        roleEntity = org;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = " + theConditional);
                }

            } catch (Exception ex) {

            }
        }
        if (practitionerRole.getPractitioner().getReference() != null) {
            roleEntity.setPractitioner(practitionerDao.readEntity(new IdType(practitionerRole.getPractitioner().getReference())));
        }

        if (practitionerRole.getOrganization().getReference() != null) {
            roleEntity.setManaginsOrganisation(organisationDao.readEntity(new IdType(practitionerRole.getOrganization().getReference())));
        }

        if (practitionerRole.getCode().size() > 0) {
            if (practitionerRole.getCode().get(0).getCoding().get(0).getSystem().equals(CareConnectSystem.SDSJobRoleName)) {
                roleEntity.setRole(codeDao.findCode(practitionerRole.getCode().get(0).getCoding().get(0).getSystem(), practitionerRole.getCode().get(0).getCoding().get(0).getCode()));
            }
        }

        for (CodeableConcept specialty : practitionerRole.getSpecialty()) {
            Boolean found = false;
            ConceptEntity specialtyConcept = codeDao.findCode(specialty.getCoding().get(0).getSystem()
                    , specialty.getCoding().get(0).getCode());
            for (ConceptEntity searchConcept : roleEntity.getSpecialties()) {
                if (searchConcept.getId().equals(specialtyConcept.getId())) found = true;
            }
            try {
                if (!found) roleEntity.getSpecialties().add(specialtyConcept);
            } catch (Exception ex) {

            }
        }
        em.persist(roleEntity);
        Boolean found = false;
        for (Identifier identifier : practitionerRole.getIdentifier()) {
            for (PractitionerRoleIdentifier identifierEntity : roleEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(identifierEntity.getSystemUri()) && identifier.getValue().equals(identifierEntity.getValue())) {
                    found = true;
                }
                if (!found) {
                    PractitionerRoleIdentifier ident = new PractitionerRoleIdentifier();
                    ident.setValue(identifier.getValue());
                    ident.setPractitionerRole(roleEntity);
                    ident.setSystem(codeSystemDao.findSystem(identifier.getSystem()));
                    em.persist(ident);
                }
            }
        }
        return practitionerRole; //roleEntity;
    }

    @Override
    public List<PractitionerRole> searchEntity(
            TokenParam identifier
            , ReferenceParam practitioner
            , ReferenceParam organisation) {
        List<PractitionerRole> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<PractitionerRole> criteria = builder.createQuery(PractitionerRole.class);
        Root<PractitionerRole> root = criteria.from(PractitionerRole.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<PractitionerRole> results = new ArrayList<>();

        if (identifier !=null)
        {

            Join<PractitionerRole, PractitionerRoleIdentifier> join = root.join("identifiers", JoinType.LEFT);

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

        for (PractitionerRole practitionerRole : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            // Practitioner practitioner = practitionerEntityToFHIRPractitionerTransformer.transform(practitionerEntity);
            results.add(practitionerRole);
        }

        return results;
    }


    @Override
    public List<org.hl7.fhir.dstu3.model.PractitionerRole> search(
            TokenParam identifier
            , ReferenceParam practitioner
            , ReferenceParam organisation) {
        return null;
    }

}
