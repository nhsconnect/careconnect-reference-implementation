package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Endpoint;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointEntity;

import java.util.List;

public interface EndpointRepository extends BaseRepository<EndpointEntity,Endpoint> {
    void save(FhirContext ctx, EndpointEntity endpoint) throws OperationOutcomeException;

    Endpoint read(FhirContext ctx, IdType theId);

    EndpointEntity readEntity(FhirContext ctx, IdType theId);

    Endpoint create(FhirContext ctx, Endpoint endpoint, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;



    List<Endpoint> searchEndpoint(FhirContext ctx,
                                  @OptionalParam(name = Endpoint.SP_IDENTIFIER) TokenParam identifier,
                                  @OptionalParam(name = Endpoint.SP_RES_ID) StringParam id

    );

    List<EndpointEntity> searchEndpointEntity(FhirContext ctx,
                                              @OptionalParam(name = Endpoint.SP_IDENTIFIER) TokenParam identifier,
                                              @OptionalParam(name = Endpoint.SP_RES_ID) StringParam id
    );
}
