package uk.nhs.careconnect.ri.dao.CodeSystem;

import org.hl7.fhir.instance.model.ValueSet;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

public interface CodeSystemRepository {

    public CodeSystemEntity findBySystem(String system);

    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptDefinitionComponent concept);

}
