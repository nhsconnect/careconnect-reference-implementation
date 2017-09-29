package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.OperationOutcome;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.daointerface.LocationRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class LocationResourceProvider implements IResourceProvider {


    @Autowired
    private LocationRepository locationDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Location.class;
    }


    @Update
    public MethodOutcome updateLocation(HttpServletRequest theRequest, @ResourceParam Location location, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        Location newLocation = locationDao.create(location, theId, theConditional);
        method.setId(newLocation.getIdElement());
        method.setResource(newLocation);



        return method;
    }

    @Search
    public List<Location> searchLocation(HttpServletRequest theRequest,
                                         @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifierCode,
                                         @OptionalParam(name = Location.SP_NAME) StringParam name) {
        return locationDao.searchLocation(identifierCode,name);
    }

    @Read()
    public Location getLocation(@IdParam IdType locationId) {

        Location location = locationDao.read(locationId);

        if ( location == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Location/ " + locationId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return location;
    }


}
