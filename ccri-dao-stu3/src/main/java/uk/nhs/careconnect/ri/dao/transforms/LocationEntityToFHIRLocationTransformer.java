package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.location.LocationAddress;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
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

        for(int f=0;f<locationEntity.getIdentifiers().size();f++)
        {
            location.addIdentifier()
                    .setSystem(locationEntity.getIdentifiers().get(f).getSystem().getUri())
                    .setValue(locationEntity.getIdentifiers().get(f).getValue());
        }


        location.setId(locationEntity.getId().toString());

        location.setName(locationEntity.getName());

        for(int f=0;f<locationEntity.getTelecoms().size();f++)
        {
            location.addTelecom()
                    .setSystem(locationEntity.getTelecoms().get(f).getSystem())
                    .setValue(locationEntity.getTelecoms().get(f).getValue())
                    .setUse(locationEntity.getTelecoms().get(f).getTelecomUse());
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
