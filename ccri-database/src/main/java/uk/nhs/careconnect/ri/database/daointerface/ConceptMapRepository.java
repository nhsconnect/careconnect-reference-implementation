package uk.nhs.careconnect.ri.database.daointerface;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.dstu3.model.ConceptMap;
import java.util.List;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapEntity;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;

public interface ConceptMapRepository extends BaseRepository<ConceptMapEntity,ConceptMap>{

//	void save(ConceptMapEntity conceptMap);


    ConceptMap create(FhirContext ctx, ConceptMap conceptMap);

  //  ConceptMap read(IdType theId) ;
 //   ValueSet create(FhirContext ctx,ValueSet valueSet)  throws OperationOutcomeException;

    ConceptMap read(FhirContext ctx,IdType theId) ;
    
    List<ConceptMap> search (FhirContext ctx,
            @OptionalParam(name = ValueSet.SP_NAME) StringParam name,
            @OptionalParam(name = ValueSet.SP_PUBLISHER) StringParam publisher,
            @OptionalParam(name = ValueSet.SP_URL) UriParam url
    );

    List<ConceptMapEntity> searchEntity (FhirContext ctx,
                             @OptionalParam(name = ValueSet.SP_NAME) StringParam name,
                             @OptionalParam(name = ValueSet.SP_PUBLISHER) StringParam publisher,
                             @OptionalParam(name = ValueSet.SP_URL) UriParam url
    );

//    List<ConceptMap> searchConceptMap (
  //          @OptionalParam(name = ConceptMap.SP_NAME) StringParam name
  //  );
}
