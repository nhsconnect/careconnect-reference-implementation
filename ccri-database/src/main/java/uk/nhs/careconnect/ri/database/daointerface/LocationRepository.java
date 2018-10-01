package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;

import java.util.List;

public interface LocationRepository extends BaseRepository<LocationEntity,Location> {
    void save(FhirContext ctx,LocationEntity location) throws OperationOutcomeException;

    Location read(FhirContext ctx, IdType theId);

    LocationEntity readEntity(FhirContext ctx,IdType theId);

    Location create(FhirContext ctx,Location location, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;



    List<Location> searchLocation(FhirContext ctx,

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name,
            @OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) StringParam postCode
            ,@OptionalParam(name= Location.SP_RES_ID) StringParam id

    );

    List<LocationEntity> searchLocationEntity (FhirContext ctx,

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name,
            @OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) StringParam postCode
            ,@OptionalParam(name= Location.SP_RES_ID) StringParam id

    );
}
