package uk.nhs.careconnect.ri.dao.CodeSystem;

import ca.uhn.fhir.rest.method.RequestDetails;
import org.hl7.fhir.instance.model.ValueSet;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

public interface CodeSystemRepository {

    public CodeSystemEntity findBySystem(String system);

    public SystemEntity findSystem(String system);

    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptDefinitionComponent concept);

    public void storeNewCodeSystemVersion(CodeSystemEntity theCodeSystemVersion, RequestDetails theRequestDetails);

    public void save(CodeSystemEntity codeSystemEntity);

    public void setProcessDeferred(boolean theProcessDeferred);

 //   void saveDeferred();

}
