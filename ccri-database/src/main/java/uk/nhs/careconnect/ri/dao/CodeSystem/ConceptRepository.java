package uk.nhs.careconnect.ri.dao.CodeSystem;

import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptDesignation;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptParentChildLink;

public interface ConceptRepository {

    public ConceptEntity findCode(String codeSystemUri, String code);

    public ConceptEntity findCode(CodeSystemEntity codeSystemUri, String code);

    public ConceptEntity save(ConceptEntity conceptEntity);

    public void save(ConceptParentChildLink conceptParentChildLink);

    public void persistLinks(ConceptEntity conceptEntity);

  //  public void storeConcepts(Map<String, ConceptEntity> code2concept, RequestDetails theRequestDetails);

    public ConceptDesignation save(ConceptDesignation conceptDesignation);




}
