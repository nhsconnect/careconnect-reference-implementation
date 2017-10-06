package uk.nhs.careconnect.ri.dao.Dstu2.CodeSystem;

import org.hl7.fhir.instance.model.ValueSet;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

public interface CodeSystemRepository {

    public CodeSystemEntity findBySystem(String system);

    public SystemEntity findSystem(String system);

    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptDefinitionComponent concept);



    public void save(CodeSystemEntity codeSystemEntity);

    public void setProcessDeferred(boolean theProcessDeferred);

 //   void saveDeferred();

}
