package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;

import java.util.List;

public interface LocationRepository  {
    void save(LocationEntity location);

    Location read(IdType theId);

    Location create(Location location, @IdParam IdType theId, @ConditionalUrlParam String theConditional);


    List<Location> searchLocation(

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    );

    List<LocationEntity> searchLocationEntity (

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    );
}
