package uk.nhs.careconnect.ri.entity;


import org.hl7.fhir.instance.model.Address;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseAddress {

    @Enumerated(EnumType.ORDINAL)
    Address.AddressUse addressUse;

    @Enumerated(EnumType.ORDINAL)
    Address.AddressType addressType;

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
