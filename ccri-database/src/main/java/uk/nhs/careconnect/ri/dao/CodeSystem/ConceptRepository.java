package uk.nhs.careconnect.ri.dao.CodeSystem;

import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

public interface ConceptRepository {

    public ConceptEntity findCode(String codeSystemUri, String code);

    public ConceptEntity addCode(String code, String display, CodeSystemEntity codeSystemEntity);

    public ConceptEntity save(ConceptEntity conceptEntity);

}
