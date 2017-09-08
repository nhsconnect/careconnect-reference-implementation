package uk.nhs.careconnect.ri.provider;


import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationResourceProvider implements IResourceProvider {



    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Location.class;
    }


    @Search
    public List<Location> getByIdentifierCode(@RequiredParam(name = Location.SP_IDENTIFIER) TokenParam identifierCode) {
      return null;
    }

    @Read()
    public Location getLocationById(@IdParam IdType locationId) {

        return null;
    }


}
