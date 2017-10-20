package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.nhs.careconnect.ri.entity.Terminology.*;

public interface ConceptRepository {

    public ConceptEntity findCode(String codeSystemUri, String code);

    public ConceptEntity findCode(CodeSystemEntity codeSystemUri, String code);

    public ConceptEntity save(ConceptEntity conceptEntity);

    public ConceptEntity saveTransactional(ConceptEntity conceptEntity);

    public void save(ConceptParentChildLink conceptParentChildLink);

    public void persistLinks(ConceptEntity conceptEntity);

    public void storeNewCodeSystemVersion(CodeSystemEntity theCodeSystemVersion, RequestDetails theRequestDetails);

    public ConceptDesignation save(ConceptDesignation conceptDesignation);

    public Session getSession();

    public CodeSystemEntity findBySystem(String system);


    public Transaction getTransaction(Session session);

  //  public void beginTransaction(Transaction tx);

    public void commitTransaction(Transaction tx);



}
