package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Duration;
import org.hl7.fhir.dstu3.model.Quantity;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptDesignation;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptParentChildLink;

public interface ConceptRepository {

    ConceptEntity findCode(Coding code);

    ConceptEntity findCode(Duration duration);

    ConceptEntity findAddCode(Coding code);

    ConceptEntity findAddCode(Quantity quantity);

    ConceptEntity findCode(CodeSystemEntity codeSystemUri, String code);

    ConceptEntity save(ConceptEntity conceptEntity) throws OperationOutcomeException;

    //public ConceptEntity saveTransactional(ConceptEntity conceptEntity);

    void save(ConceptParentChildLink conceptParentChildLink) throws OperationOutcomeException;

    void persistLinks(ConceptEntity conceptEntity) throws OperationOutcomeException;

    void storeNewCodeSystemVersion(CodeSystemEntity theCodeSystemVersion, RequestDetails theRequestDetails) throws OperationOutcomeException;

    ConceptDesignation save(ConceptDesignation conceptDesignation) throws OperationOutcomeException;

    Session getSession();

    public CodeSystemEntity findBySystem(String system);


    public Transaction getTransaction(Session session);

  //  public void beginTransaction(Transaction tx);

    public void commitTransaction(Transaction tx);



}
