package uk.nhs.careconnect.ri.dao.Location;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Location;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;

@Component
public class LocationEntityToFHIRLocationTransformer implements Transformer<LocationEntity, Location> {

    @Override
    public Location transform(final LocationEntity locationEntity) {
        final Location location = new Location();



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
            location.addIdentifier()
                    .setSystem(locationEntity.getTelecoms().get(f).getSystem().getUri())
                    .setValue(locationEntity.getTelecoms().get(f).getValue())
                    .setUse(locationEntity.getTelecoms().get(f).getUse());
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
            location.setAddress(adr);
        }

        return location;

    }
}
