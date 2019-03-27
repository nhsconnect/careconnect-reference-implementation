package uk.nhs.careconnect.ri.database.entity;


import org.hl7.fhir.dstu3.model.Address;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseAddress extends BaseResource {

    @Enumerated(EnumType.ORDINAL)
    Address.AddressUse addressUse;

    @Enumerated(EnumType.ORDINAL)
    Address.AddressType addressType;

    public abstract AddressEntity getAddress();

    public abstract AddressEntity setAddress(AddressEntity addressEntity);

    public Address.AddressUse getAddressUse() {
        return addressUse;
    }



    public void setAddressUse(Address.AddressUse addressUse) {
        this.addressUse = addressUse;
    }



    public Address.AddressType getAddressType() {
        return addressType;
    }



    public void setAddressType(Address.AddressType addressType) {
        this.addressType = addressType;
    }
}
