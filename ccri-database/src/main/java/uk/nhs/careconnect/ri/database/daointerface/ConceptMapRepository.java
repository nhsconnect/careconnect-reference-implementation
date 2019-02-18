package uk.nhs.careconnect.ri.database.daointerface;


import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ConceptMap;
import java.util.List;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapEntity; 

public interface ConceptMapRepository {

//	void save(ConceptMapEntity conceptMap);


    ConceptMap create(ConceptMap conceptMap);

   // ConceptMap read(IdType theId) ;

//    List<ConceptMap> searchConceptMap (
  //          @OptionalParam(name = ConceptMap.SP_NAME) StringParam name
  //  );
}
