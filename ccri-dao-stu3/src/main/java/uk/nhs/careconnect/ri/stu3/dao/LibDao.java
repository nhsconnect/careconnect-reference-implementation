package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.BaseIdentifier2;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemUniqueId;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

@Component
public class LibDao {

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    PatientRepository patientDao;

    @Autowired
    @Lazy
    OrganisationRepository organisationDao;

    @Autowired
    @Lazy
    PractitionerRepository practitionerDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    private static final Logger log = LoggerFactory.getLogger(LibDao.class);

    public BaseIdentifier setIdentifier(Identifier identifier, BaseIdentifier entityIdentifier) throws OperationOutcomeException {

        if (identifier.hasType()) {
            ConceptEntity code = conceptDao.findAddCode(identifier.getType().getCoding().get(0));
            if (code != null) {
                entityIdentifier.setIdentifierType(code);
            } else {
                log.info("IdentifierType: Missing System/Code = " + identifier.getType().getCoding().get(0).getSystem() + " code = " + identifier.getType().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = " + identifier.getType().getCoding().get(0).getSystem() + " code = " + identifier.getType().getCoding().get(0).getCode());
            }

        }
        if (identifier.hasValue()) {
            entityIdentifier.setValue(daoutils.removeSpace(identifier.getValue()));
        }

        if (identifier.hasSystem()) {
            entityIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
        } else {
            entityIdentifier.setSystem(null);
        }
        if (identifier.hasUse()) {
            entityIdentifier.setUse(identifier.getUse());
        }

        return entityIdentifier;
    }

    public BaseIdentifier2 setIdentifier2(Identifier identifier, BaseIdentifier2 entityIdentifier) throws OperationOutcomeException {


        if (identifier.hasValue()) {
            entityIdentifier.setValue(daoutils.removeSpace(identifier.getValue()));
        }


        if (identifier.hasSystem()) {
            entityIdentifier.setSystem(findUniqueId(identifier.getSystem()));
        } else {
            entityIdentifier.setSystem(null);
        }

        if (identifier.hasUse()) {
            entityIdentifier.setUse(identifier.getUse());
        }

        // REVISIT Assume if system not found, it's not supported so do not store
        if (entityIdentifier.getSystem()== null && identifier.hasSystem()) return null;

        return entityIdentifier;
    }


    public NamingSystemUniqueId findUniqueId(String system) throws OperationOutcomeException {

        if (system==null || system.isEmpty()) {
            throw new OperationOutcomeException("System is required","System is required", OperationOutcome.IssueType.INVALID);
        }
        CriteriaBuilder builder = em.getCriteriaBuilder();

        NamingSystemUniqueId systemEntity = null;
        CriteriaQuery<NamingSystemUniqueId> criteria = builder.createQuery(NamingSystemUniqueId.class);

        Root<NamingSystemUniqueId> root = criteria.from(NamingSystemUniqueId.class);
        List<Predicate> predList = new LinkedList<Predicate>();
        log.debug("Looking for System = " + system);

        Predicate p = builder.equal(root.<String>get("value"),system);
        predList.add(p);
        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            log.debug("Found System "+system);
            criteria.select(root).where(predArray);

            List<NamingSystemUniqueId> qryResults = em.createQuery(criteria).getResultList();

            for (NamingSystemUniqueId cme : qryResults) {
                systemEntity = cme;
                break;
            }
        }

        return systemEntity;
    }

    public OrganisationEntity findOrganisationEntity(FhirContext ctx, Reference ref) {
        OrganisationEntity organisationEntity = null;
        if (ref.hasReference()) {
            log.trace(ref.getReference());
            organisationEntity = organisationDao.readEntity(ctx, new IdType(ref.getReference()));

        }
        if (ref.hasIdentifier()) {
            // This copes with reference.identifier param (a short cut?)
            log.trace(ref.getIdentifier().getSystem() + " " + ref.getIdentifier().getValue());
            organisationEntity = organisationDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
        }
        return organisationEntity;
    }

    public PatientEntity findPatientEntity(FhirContext ctx, Reference ref) {
        PatientEntity patientEntity = null;
        if (ref.hasReference()) {
            log.trace(ref.getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(ref.getReference()));

        }
        if (ref.hasIdentifier()) {
            // This copes with reference.identifier param (a short cut?)
            log.trace(ref.getIdentifier().getSystem() + " " + ref.getIdentifier().getValue());
            patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
        }
        return patientEntity;
    }

    public PractitionerEntity findPractitionerEntity(FhirContext ctx, Reference ref) {
        PractitionerEntity practitionerEntity = null;
        if (ref.hasReference()) {
            log.trace(ref.getReference());
            practitionerEntity = practitionerDao.readEntity(ctx, new IdType(ref.getReference()));

        }
        if (ref.hasIdentifier()) {
            // This copes with reference.identifier param (a short cut?)
            log.trace(ref.getIdentifier().getSystem() + " " + ref.getIdentifier().getValue());
            practitionerEntity = practitionerDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
        }
        return practitionerEntity;
    }

    public Extension getResourceTypeExt(String resourceType) {
        UriType uri = new UriType();
        uri.setValue(resourceType);
        Extension uriExt = new Extension().setUrl("http://fhir.gov.uk/StructureDefinition/resourceType");
        return uriExt;
    }
}
