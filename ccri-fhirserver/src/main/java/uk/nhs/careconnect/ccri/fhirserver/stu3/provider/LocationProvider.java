package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.LocationRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class LocationProvider implements ICCResourceProvider {


    @Autowired
    private LocationRepository locationDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Location.class;
    }

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return locationDao.count();
    }

    @Update
    public MethodOutcome updateLocation(HttpServletRequest theRequest, @ResourceParam Location location, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

    try {
        Location newLocation = locationDao.create(ctx, location, theId, theConditional);
        method.setId(newLocation.getIdElement());
        method.setResource(newLocation);

    } catch (Exception ex) {

        ProviderResponseLibrary.handleException(method,ex);
    }


        return method;
    }

    @Create
    public MethodOutcome createLocation(HttpServletRequest theRequest, @ResourceParam Location location) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
        Location newLocation = locationDao.create(ctx, location,null,null);
        method.setId(newLocation.getIdElement());
        method.setResource(newLocation);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Search
    public List<Location> searchLocation(HttpServletRequest theRequest,
                                         @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifierCode,
                                         @OptionalParam(name = Location.SP_NAME) StringParam name,
                                         @OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) StringParam postCode
            , @OptionalParam(name = Location.SP_RES_ID) StringParam resid
    ) {
        return locationDao.searchLocation(ctx, identifierCode,name,postCode,resid);
    }

    @Read()
    public Location getLocation(@IdParam IdType locationId) {
    	resourcePermissionProvider.checkPermission("read");
        Location location = locationDao.read(ctx,locationId);

        if ( location == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Location/ " + locationId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return location;
    }


    @Validate
    public MethodOutcome testResource(@ResourceParam Location resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}
