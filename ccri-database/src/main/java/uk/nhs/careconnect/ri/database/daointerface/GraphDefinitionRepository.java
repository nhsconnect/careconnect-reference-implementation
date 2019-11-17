package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.GraphDefinition;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.graphDefinition.GraphDefinitionEntity;
import uk.nhs.careconnect.ri.database.entity.graphDefinition.GraphDefinitionEntity;

import java.util.List;


public interface GraphDefinitionRepository extends BaseRepository<GraphDefinitionEntity,GraphDefinition> {


    void save(FhirContext ctxR3,GraphDefinitionEntity graphDefinitionEntity);

    GraphDefinition create(FhirContext ctxR3,FhirContext ctxR4, GraphDefinition graphDefinition)  throws OperationOutcomeException;

    org.hl7.fhir.r4.model.GraphDefinition create(FhirContext ctxR3,FhirContext ctxR4, org.hl7.fhir.r4.model.GraphDefinition graphDefinition)  throws OperationOutcomeException;

    GraphDefinition read(FhirContext ctxR3, IdType theId) ;

    org.hl7.fhir.r4.model.GraphDefinition read(FhirContext ctxR4, org.hl7.fhir.r4.model.IdType theId) ;

    List<GraphDefinition> search(FhirContext ctxR3,
                          @OptionalParam(name = GraphDefinition.SP_NAME) StringParam name,
                          @OptionalParam(name = GraphDefinition.SP_PUBLISHER) StringParam publisher,
                          @OptionalParam(name = GraphDefinition.SP_URL) UriParam url
    );

    List<org.hl7.fhir.r4.model.GraphDefinition> searchR4(FhirContext ctxR4,
                                 @OptionalParam(name = org.hl7.fhir.r4.model.GraphDefinition.SP_NAME) StringParam name,
                                 @OptionalParam(name = org.hl7.fhir.r4.model.GraphDefinition.SP_PUBLISHER) StringParam publisher,
                                 @OptionalParam(name = org.hl7.fhir.r4.model.GraphDefinition.SP_URL) UriParam url
    );
}
