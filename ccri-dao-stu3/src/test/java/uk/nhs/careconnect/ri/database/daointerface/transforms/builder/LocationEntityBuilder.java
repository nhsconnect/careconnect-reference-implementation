package uk.nhs.careconnect.ri.database.daointerface.transforms.builder;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Location;
import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationAddress;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationTelecom;

import java.util.ArrayList;
import java.util.List;

public class LocationEntityBuilder {

    private Long id = 1001L;
    private String name;
    private Location.LocationStatus status = Location.LocationStatus.ACTIVE;
    private List<LocationAddress> addresses = new ArrayList<>();
    private List<LocationTelecom> telecoms = new ArrayList<>();

    public LocationEntityBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public LocationEntityBuilder addAddress(String addressLine1, String addressLine2,
                                            String addressLine3, String city,
                                            String county, String postcode) {
        LocationAddress locationAddress = new LocationAddress();
        locationAddress.setAddressType(Address.AddressType.BOTH);
        locationAddress.setAddressUse(Address.AddressUse.HOME);
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setAddress1(addressLine1);
        addressEntity.setAddress2(addressLine2);
        addressEntity.setAddress3(addressLine3);
        addressEntity.setCity(city);
        addressEntity.setCounty(county);
        addressEntity.setPostcode(postcode);
        locationAddress.setAddress(addressEntity);
        addresses.add(locationAddress);
        return this;
    }

    public LocationEntityBuilder addHomePhone(String phoneNumber) {
        LocationTelecom telecom = new LocationTelecom();
        telecom.setTelecomUse(ContactPoint.ContactPointUse.HOME);
        telecom.setValue(phoneNumber);
        telecoms.add(telecom);
        return this;
    }

    public LocationEntity build() {
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setId(id);
        locationEntity.setName(name);
        locationEntity.setStatus(status);
        locationEntity.getAddresses();
        for (LocationAddress address : addresses) {
            locationEntity.addAddress(address);
        }
        locationEntity.getTelecoms();
        for (LocationTelecom telecom : telecoms) {
            locationEntity.addTelecom(telecom);
        }
        return locationEntity;
    }
}
