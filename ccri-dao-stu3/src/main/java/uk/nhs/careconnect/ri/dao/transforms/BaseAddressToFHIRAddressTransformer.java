package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;


@Component
public class BaseAddressToFHIRAddressTransformer implements Transformer<BaseAddress, Address> {

    @Override
    public Address transform(BaseAddress baseAddress) {

        Address address= new Address();
        AddressEntity addressEntity = baseAddress.getAddress();
        if (addressEntity.getAddress1()!=null && !addressEntity.getAddress1().isEmpty())
        {
            address.addLine(addressEntity.getAddress1().trim());
        }
        if (addressEntity.getAddress2()!=null && !addressEntity.getAddress2().isEmpty())
        {
            address.addLine(addressEntity.getAddress2().trim());
        }
        if (addressEntity.getAddress3()!=null && !addressEntity.getAddress3().isEmpty())
        {
            address.addLine(addressEntity.getAddress3().trim());
        }
        if (addressEntity.getAddress4()!=null && !addressEntity.getAddress4().isEmpty())
        {
            address.addLine(addressEntity.getAddress4().trim());
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
        if (addressEntity.getCountry() != null) {
            address.setCountry(addressEntity.getCountry());
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
