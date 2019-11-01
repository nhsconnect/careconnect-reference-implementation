package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MessageDefinition;
import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.messageDefinition.MessageDefinitionEntity;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;

import java.util.List;


public interface MessageDefinitionRepository extends BaseRepository<MessageDefinitionEntity, MessageDefinition> {



    void save(FhirContext ctx, MessageDefinitionEntity valueset);


    MessageDefinition create(FhirContext ctx, MessageDefinition messageDefinition)  throws OperationOutcomeException;

    MessageDefinition read(FhirContext ctx, IdType theId) ;


    List<MessageDefinition> search(FhirContext ctx,
                          @OptionalParam(name = MessageDefinition.SP_NAME) StringParam name,
                          @OptionalParam(name = MessageDefinition.SP_PUBLISHER) StringParam publisher,
                          @OptionalParam(name = MessageDefinition.SP_URL) UriParam url,
                          @OptionalParam(name = MessageDefinition.SP_IDENTIFIER) TokenParam identifier
    );

    List<MessageDefinitionEntity> searchEntity(FhirContext ctx,
                                   @OptionalParam(name = MessageDefinition.SP_NAME) StringParam name,
                                   @OptionalParam(name = MessageDefinition.SP_PUBLISHER) StringParam publisher,
                                   @OptionalParam(name = MessageDefinition.SP_URL) UriParam url,
                                   @OptionalParam(name = MessageDefinition.SP_IDENTIFIER) TokenParam identifier
    );

}
