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

    public org.hl7.fhir.instance.model.Address.AddressUse getAddressUseDstu2() {
        switch (this.addressUse) {
            case HOME:
                return org.hl7.fhir.instance.model.Address.AddressUse.HOME;
            case WORK:
                return org.hl7.fhir.instance.model.Address.AddressUse.WORK;
            case TEMP:
                return org.hl7.fhir.instance.model.Address.AddressUse.TEMP;
            case OLD:
                return org.hl7.fhir.instance.model.Address.AddressUse.OLD;
            default:
                return null;
        }
    }

    public void setAddressUse(Address.AddressUse addressUse) {
        this.addressUse = addressUse;
    }

    public void setAddressUseDstu2(org.hl7.fhir.instance.model.Address.AddressUse addressUse) {
        switch (addressUse) {
            case OLD: {
                this.addressUse = Address.AddressUse.OLD;
                break;
            }
            case HOME: {
                this.addressUse = Address.AddressUse.HOME;
                break;
            }
            case TEMP: {
                this.addressUse = Address.AddressUse.TEMP;
                break;
            }
            case WORK: {
                this.addressUse = Address.AddressUse.WORK;
                break;
            }

        }
    }

    public Address.AddressType getAddressType() {
        return addressType;
    }

    public org.hl7.fhir.instance.model.Address.AddressType getAddressTypeDstu2() {

        switch (this.addressType) {
            case PHYSICAL:
                return org.hl7.fhir.instance.model.Address.AddressType.PHYSICAL;
            case BOTH:
                return org.hl7.fhir.instance.model.Address.AddressType.BOTH;
            case NULL:
                return org.hl7.fhir.instance.model.Address.AddressType.NULL;
            case POSTAL:
                return org.hl7.fhir.instance.model.Address.AddressType.POSTAL;
            default:
                return null;
        }
    }

    public void setAddressType(Address.AddressType addressType) {
        this.addressType = addressType;
    }
}
