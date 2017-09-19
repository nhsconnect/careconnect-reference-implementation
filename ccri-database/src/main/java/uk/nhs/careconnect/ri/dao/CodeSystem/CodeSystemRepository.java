package uk.nhs.careconnect.ri.dao.CodeSystem;

import ca.uhn.fhir.rest.method.RequestDetails;
import org.hl7.fhir.instance.model.ValueSet;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

public interface CodeSystemRepository {

    public CodeSystemEntity findBySystem(String system);

    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptDefinitionComponent concept);

    public void storeNewCodeSystemVersion(String theSystem, CodeSystemEntity theCodeSystemVersion, RequestDetails theRequestDetails);

    public void setProcessDeferred(boolean theProcessDeferred);

 //   void saveDeferred();

}
