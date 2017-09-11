package uk.nhs.careconnect.ri.dao.Location;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.ContactPoint;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Meta;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;

@Component
public class LocationEntityToFHIRLocationTransformer implements Transformer<LocationEntity, Location> {

    @Override
    public Location transform(final LocationEntity locationEntity) {
        final Location location = new Location();

        Meta meta = new Meta().addProfile(CareConnectProfile.Organization_1);

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
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(locationEntity.getTelecoms().get(f).getValue())
                    .setUse(locationEntity.getTelecoms().get(f).getTelecomUse());
        }


        location.setId(locationEntity.getId().toString());


        for(int f=0;f<locationEntity.getAddresses().size();f++)
        {
            AddressEntity adressEnt = locationEntity.getAddresses().get(f).getAddress();

            Address adr= new Address();
            if (adressEnt.getAddress1()!="")
            {
                adr.addLine(adressEnt.getAddress1());
            }
            if (adressEnt.getAddress2()!="")
            {
                adr.addLine(adressEnt.getAddress2());
            }
            if (adressEnt.getAddress3()!="")
            {
                adr.addLine(adressEnt.getAddress3());
            }
            if (adressEnt.getAddress4()!="")
            {
                adr.addLine(adressEnt.getAddress4());
            }
            if (adressEnt.getPostcode() !=null)
            {
                adr.setPostalCode(adressEnt.getPostcode());
            }
            if (adressEnt.getCity() != null) {
                adr.setCity(adressEnt.getCity());
            }
            if (adressEnt.getCounty() != null) {
                adr.setDistrict(adressEnt.getCounty());
            }
            if (locationEntity.getAddresses().get(f).getAddressType() != null) {
                adr.setType(locationEntity.getAddresses().get(f).getAddressType());
            }
            if (locationEntity.getAddresses().get(f).getAddressUse() != null) {
                adr.setUse(locationEntity.getAddresses().get(f).getAddressUse());
            }
            location.setAddress(adr);
        }

        return location;

    }
}
