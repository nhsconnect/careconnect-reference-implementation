package uk.nhs.careconnect.ri.daointerface.Transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.BaseAddress;


@Component
public class BaseAddressToFHIRAddressTransformer implements Transformer<BaseAddress, Address> {

    @Override
    public Address transform(BaseAddress baseAddress) {

        Address address= new Address();
        AddressEntity addressEntity = baseAddress.getAddress();
        if (addressEntity.getAddress1()!="")
        {
            address.addLine(addressEntity.getAddress1());
        }
        if (addressEntity.getAddress2()!="")
        {
            address.addLine(addressEntity.getAddress2());
        }
        if (addressEntity.getAddress3()!="")
        {
            address.addLine(addressEntity.getAddress3());
        }
        if (addressEntity.getAddress4()!="")
        {
            address.addLine(addressEntity.getAddress4());
        }
        if (addressEntity.getPostcode() !=null)
        {
            address.setPostalCode(addressEntity.getPostcode());
        }
        if (addressEntity.getCity() != null) {
            address.setCity(addressEntity.getCity());
        }
        if (addressEntity.getCounty() != null) {
            address.setDistrict(addressEntity.getCounty());
        }

        if (baseAddress.getAddressType() != null) {
            address.setType(baseAddress.getAddressType());
        }
        if (baseAddress.getAddressUse() != null) {
            address.setUse(baseAddress.getAddressUse());
        }

        return address;
    }
}
