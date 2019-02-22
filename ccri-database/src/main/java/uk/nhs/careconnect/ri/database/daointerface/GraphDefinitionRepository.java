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


public interface GraphDefinitionRepository {


    void save(FhirContext ctx, GraphDefinitionEntity valueset);

    GraphDefinition create(FhirContext ctx, GraphDefinition graphDefinition)  throws OperationOutcomeException;

    GraphDefinition read(FhirContext ctx, IdType theId) ;

    List<GraphDefinition> search(FhirContext ctx,
                          @OptionalParam(name = GraphDefinition.SP_NAME) StringParam name,
                          @OptionalParam(name = GraphDefinition.SP_PUBLISHER) StringParam publisher,
                          @OptionalParam(name = GraphDefinition.SP_URL) UriParam url

    );

}
