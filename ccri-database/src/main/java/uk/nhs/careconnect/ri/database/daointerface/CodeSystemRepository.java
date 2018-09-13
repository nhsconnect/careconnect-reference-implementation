package uk.nhs.careconnect.ri.database.daointerface;


import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.SystemEntity;

public interface CodeSystemRepository {

    public CodeSystemEntity findBySystem(String system);

    public SystemEntity findSystem(String system) throws OperationOutcomeException;

    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptReferenceComponent concept);



    public void save(CodeSystemEntity codeSystemEntity);

    public void setProcessDeferred(boolean theProcessDeferred);

 //   void saveDeferred();

}
