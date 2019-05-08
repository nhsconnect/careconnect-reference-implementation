package uk.nhs.careconnect.ri.r4.dao;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Identifier.IdentifierUse;
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
public class LibDaoR4 {

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    private static final Logger log = LoggerFactory.getLogger(LibDaoR4.class);

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
            entityIdentifier.setValue(daoutilsR4.removeSpace(identifier.getValue()));
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

    public BaseIdentifier setIdentifier(org.hl7.fhir.r4.model.Identifier identifier, BaseIdentifier entityIdentifier) throws OperationOutcomeException {

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
            entityIdentifier.setValue(daoutilsR4.removeSpace(identifier.getValue()));
        }

        if (identifier.hasSystem()) {
            entityIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
        } else {
            entityIdentifier.setSystem(null);
        }
        if (identifier.hasUse()) {
            entityIdentifier.setUse(convertIdentifier(identifier.getUse()));
        }

        return entityIdentifier;
    }

    public static Identifier.IdentifierUse convertIdentifier(org.hl7.fhir.r4.model.Identifier.IdentifierUse use) {
        switch (use) {
            case OLD:
            case SECONDARY:
                 return Identifier.IdentifierUse.SECONDARY;

            case NULL:
                 return Identifier.IdentifierUse.NULL;

            case OFFICIAL:
                 return Identifier.IdentifierUse.OFFICIAL;

            case TEMP:
                return Identifier.IdentifierUse.TEMP;

            case USUAL:
                return Identifier.IdentifierUse.USUAL;

        }
        return null;
    }
    public static org.hl7.fhir.r4.model.Identifier.IdentifierUse convertIdentifier(IdentifierUse use) {
        switch (use) {
            case SECONDARY:
                return org.hl7.fhir.r4.model.Identifier.IdentifierUse.SECONDARY;

            case NULL:
                return org.hl7.fhir.r4.model.Identifier.IdentifierUse.NULL;

            case OFFICIAL:
                return org.hl7.fhir.r4.model.Identifier.IdentifierUse.OFFICIAL;

            case TEMP:
                return org.hl7.fhir.r4.model.Identifier.IdentifierUse.TEMP;

            case USUAL:
                return org.hl7.fhir.r4.model.Identifier.IdentifierUse.USUAL;

        }
        return null;
    }
}
