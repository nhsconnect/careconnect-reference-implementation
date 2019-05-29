package uk.nhs.careconnect.ri.stu3.dao;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.BaseIdentifier2;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemUniqueId;

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

    public Extension getResourceTypeExt(String resourceType) {
        UriType uri = new UriType();
        uri.setValue(resourceType);
        Extension uriExt = new Extension().setUrl("http://fhir.gov.uk/StructureDefinition/resourceType");
        return uriExt;
    }
}
