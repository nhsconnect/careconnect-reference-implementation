package uk.nhs.careconnect.ri.stu3.dao;

import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

@Component
public class LibDao {

    @Autowired
    private CodeSystemRepository codeSystemSvc;

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
}
