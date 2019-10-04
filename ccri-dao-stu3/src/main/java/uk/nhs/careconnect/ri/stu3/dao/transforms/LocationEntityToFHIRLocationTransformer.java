package uk.nhs.careconnect.ri.stu3.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.stu3.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.location.LocationAddress;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationIdentifier;
import uk.nhs.careconnect.ri.database.entity.location.LocationTelecom;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class LocationEntityToFHIRLocationTransformer implements Transformer<LocationEntity, Location> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocationEntityToFHIRLocationTransformer.class);


    public LocationEntityToFHIRLocationTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public Location transform(final LocationEntity locationEntity) {
        final Location location = new Location();

        Meta meta = new Meta().addProfile(CareConnectProfile.Location_1);

        if (locationEntity.getUpdated() != null) {
            meta.setLastUpdated(locationEntity.getUpdated());
        }
        else {
            if (locationEntity.getCreated() != null) {
                meta.setLastUpdated(locationEntity.getCreated());
            }
        }
        location.setMeta(meta);

        for(LocationIdentifier identifier : locationEntity.getIdentifiers())
        {
            Identifier ident = location.addIdentifier();
            ident = daoutils.getIdentifier(identifier, ident);
        }


        location.setId(locationEntity.getId().toString());

        location.setName(locationEntity.getName());

        for(LocationTelecom locationTelecom : locationEntity.getTelecoms())
        {
            location.addTelecom()
                    .setSystem(locationTelecom.getSystem())
                    .setValue(locationTelecom.getValue())
                    .setUse(locationTelecom.getTelecomUse());
        }

        for (LocationAddress locationAddress : locationEntity.getAddresses()){
            Address adr= addressTransformer.transform(locationAddress);
            location.setAddress(adr);
        }

        if (locationEntity.getManagingOrganisation() != null) {
            location.setManagingOrganization(new Reference("Organization/"+locationEntity.getManagingOrganisation().getId()));
            location.getManagingOrganization().setDisplay(locationEntity.getManagingOrganisation().getName());
        }
        if (locationEntity.getType()!=null) {
            location.getType().addCoding()
                    .setCode(locationEntity.getType().getCode())
                    .setDisplay(locationEntity.getType().getDisplay())
                    .setSystem(locationEntity.getType().getSystem());
        }
        if (locationEntity.getPhysicalType()!=null) {
            location.getPhysicalType().addCoding()
                    .setCode(locationEntity.getPhysicalType().getCode())
                    .setDisplay(locationEntity.getPhysicalType().getDisplay())
                    .setSystem(locationEntity.getPhysicalType().getSystem());
        }
        if (locationEntity.getPartOf() !=null) {
            location.setPartOf(new Reference("Location/"+locationEntity.getPartOf().getId()))
            .getPartOf().setDisplay(locationEntity.getPartOf().getName());
        }

        if (locationEntity.getAltitude() != null) {
            location.getPosition().setAltitude(locationEntity.getAltitude());
        }
        if (locationEntity.getLongitude() != null) {
            location.getPosition().setLongitude(locationEntity.getLongitude());
        }
        if (locationEntity.getLatitude() != null) {
            location.getPosition().setLatitude(locationEntity.getLatitude());
        }


        return location;

    }
}
