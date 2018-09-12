package uk.nhs.careconnect.ri.database.daointerface.transforms.builder;

import org.hl7.fhir.dstu3.model.Address;
import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.patient.PatientAddress;

public class PatientAddressBuilder {

    private Address.AddressUse addressUse = Address.AddressUse.HOME;
    private Address.AddressType addressType = Address.AddressType.BOTH;
    private AddressEntity addressEntity;

    public PatientAddressBuilder() {
        addressEntity = createAddressEntity("121b Baker Street", "Marylebone",
                null, "London", "Middlesex", "W1 2TW");
    }

    public BaseAddress build() {
        BaseAddress address = new PatientAddress();
        address.setAddress(addressEntity);
        address.setAddressUse(addressUse);
        address.setAddressType(addressType);
        return address;
    }

    public PatientAddressBuilder setAddress(String addressLine1, String addressLine2,
                                            String addressLine3, String city,
                                            String county, String postcode){
        addressEntity = createAddressEntity(addressLine1, addressLine2,
                                        addressLine3, city, county, postcode);
        return this;
    }

    public AddressEntity createAddressEntity(String addressLine1, String addressLine2,
                                             String addressLine3, String city,
                                             String county, String postcode) {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setAddress1(addressLine1);
        addressEntity.setAddress2(addressLine2);
        addressEntity.setAddress3(addressLine3);
        addressEntity.setCity(city);
        addressEntity.setCounty(county);
        addressEntity.setPostcode(postcode);
        return addressEntity;
    }

    public PatientAddressBuilder setAddressUse(Address.AddressUse addressUse) {
        this.addressUse = addressUse;
        return this;
    }

    public PatientAddressBuilder setAddressType(Address.AddressType addressType) {
        this.addressType = addressType;
        return this;
    }
}
