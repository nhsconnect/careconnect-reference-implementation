package uk.nhs.careconnect.ri.database.daointerface;


import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.jpa.entity.TermConceptMapGroupElement;
//import ca.uhn.fhir.jpa.entity.TermConceptMapGroupElementTarget;
//import ca.uhn.fhir.jpa.term.TranslationRequest;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.TranslationRequests;
import uk.nhs.careconnect.ri.database.entity.codeSystem.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.SystemEntity;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroupTarget;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;

import java.util.List;

public interface CodeSystemRepository extends BaseRepository<CodeSystemEntity,CodeSystem> {

    CodeSystemEntity findBySystem(String system);

    SystemEntity findSystem(String system) throws OperationOutcomeException;

    CodeSystem read(FhirContext ctx, IdType theId) ;

    CodeSystem create(FhirContext ctx,CodeSystem codeSystem);

    void save(FhirContext ctx, CodeSystemEntity codeSystemEntity);

    List<CodeSystem> search (FhirContext ctx,
                             @OptionalParam(name = CodeSystem.SP_NAME) StringParam name,
                             @OptionalParam(name = CodeSystem.SP_PUBLISHER) StringParam publisher,
                             @OptionalParam(name = CodeSystem.SP_URL) UriParam url
    );
    public void setProcessDeferred(boolean theProcessDeferred);
    //List<TermConceptMapGroupElementTarget> translate(TranslationRequest theTranslationRequest);
    public List<ConceptMapGroupTarget> translate(TranslationRequests theTranslationRequests); 
	//List<TermConceptMapGroupElement> translateWithReverse(TranslationRequest theTranslationRequest);

 //   void saveDeferred();

}
