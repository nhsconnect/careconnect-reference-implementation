package uk.nhs.careconnect.ri.dao.Dstu2.Location;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.org.hl7.fhir.core.Dstu2.CareConnectProfile;

@Component
public class LocationEntityToFHIRLocationTransformer implements Transformer<LocationEntity, Location> {

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
                    .setSystem(locationEntity.getTelecoms().get(f).getSystemDstu2())
                    .setValue(locationEntity.getTelecoms().get(f).getValue())
                    .setUse(locationEntity.getTelecoms().get(f).getTelecomUseDstu2());
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
                adr.setType(locationEntity.getAddresses().get(f).getAddressTypeDstu2());
            }
            if (locationEntity.getAddresses().get(f).getAddressUse() != null) {
                adr.setUse(locationEntity.getAddresses().get(f).getAddressUseDstu2());
            }
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

        return location;

    }
}
