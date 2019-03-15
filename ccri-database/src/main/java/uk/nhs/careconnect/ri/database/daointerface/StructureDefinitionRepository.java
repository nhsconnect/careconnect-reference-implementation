package uk.nhs.careconnect.ri.database.daointerface;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.structureDefinition.StructureDefinitionEntity;

import java.util.List;

public interface StructureDefinitionRepository extends BaseRepository<StructureDefinitionEntity, StructureDefinition>{

//	void save(StructureDefinitionEntity structureDefinition);


    StructureDefinition create(FhirContext ctx, StructureDefinition structuredDefinition) throws OperationOutcomeException;

  //  StructureDefinition read(IdType theId) ;
 //   ValueSet create(FhirContext ctx,ValueSet valueSet)  throws OperationOutcomeException;

    StructureDefinition read(FhirContext ctx, IdType theId) ;

    OperationOutcome delete(FhirContext ctx, IdType theId) ;

    List<StructureDefinition> search(FhirContext ctx,
                            @OptionalParam(name = ValueSet.SP_NAME) StringParam name,
                            @OptionalParam(name = ValueSet.SP_PUBLISHER) StringParam publisher,
                            @OptionalParam(name = ValueSet.SP_URL) UriParam url
    );

    List<StructureDefinitionEntity> searchEntity(FhirContext ctx,
                                                 @OptionalParam(name = ValueSet.SP_NAME) StringParam name,
                                                 @OptionalParam(name = ValueSet.SP_PUBLISHER) StringParam publisher,
                                                 @OptionalParam(name = ValueSet.SP_URL) UriParam url
    );

//    List<StructureDefinition> searchStructureDefinition (
  //          @OptionalParam(name = StructureDefinition.SP_NAME) StringParam name
  //  );
}
