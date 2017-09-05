package uk.nhs.careconnect.ri.provider.location;


import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.location.LocationSearch;
import uk.nhs.careconnect.ri.model.location.LocationDetails;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationResourceProvider implements IResourceProvider {

    @Autowired
    private LocationSearch locationSearch;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Location.class;
    }

    @Search
    public List<Location> getByIdentifierCode(@RequiredParam(name = Location.SP_IDENTIFIER) TokenParam identifierCode) {
        List<LocationDetails> locationDetails = CareConnectSystem.ODSOrganisationCode.equalsIgnoreCase(identifierCode.getSystem())
                ? locationSearch.findLocationDetailsByOrgOdsCode(identifierCode.getValue())
                : locationSearch.findLocationDetailsBySiteOdsCode(identifierCode.getValue());

        if (locationDetails.isEmpty()) {
            String msg = String.format("No location details found for code: %s", identifierCode.getValue());
            OperationOutcome operationalOutcome = new OperationOutcome();
           // TODO  operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails(msg);
            throw new InternalErrorException(msg, operationalOutcome);
        }

        return locationDetails.stream()
                .map(LocationResourceProvider::locationDetailsToLocation)
                .collect(Collectors.toList());
    }

    @Read()
    public Location getLocationById(@IdParam IdType locationId) {
        LocationDetails locationDetails = locationSearch.findLocationById(locationId.getIdPart());

        if (locationDetails == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
          // TODO  operationalOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setDetails("No location details found for location ID: "+locationId.getIdPart());
            throw new InternalErrorException("No location details found for location ID: "+locationId.getIdPart(), operationalOutcome);
        }

        return locationDetailsToLocation(locationDetails);
    }

    private static Location locationDetailsToLocation(LocationDetails locationDetails) {
        Location location = new Location();
        location.setId(new IdDt(locationDetails.getId()));
        location.getMeta().setLastUpdated(locationDetails.getLastUpdated());
        location.getMeta().setVersionId(String.valueOf(locationDetails.getLastUpdated().getTime()));
        location.getMeta().addProfile(CareConnectProfile.Location_1);
        location.setName(locationDetails.getName());
        location.addIdentifier(new Identifier().setValue(locationDetails.getSiteOdsCode()).setSystem(CareConnectSystem.ODSSiteCode));
                // TODO . locationDetails.getSiteOdsCodeName())));
        return location;
    }
}
