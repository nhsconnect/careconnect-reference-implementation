package uk.nhs.careconnect.ri.database.daointerface.transforms.builder;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.HumanName;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerAddress;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerName;
import uk.nhs.careconnect.ri.database.entity.AddressEntity;

import java.util.ArrayList;
import java.util.List;

public class PractitionerEntityBuilder {

    public static final long DEFAULT_ID = 200001L;
    private List<PractitionerName> names = new ArrayList<>();
    private List<PractitionerAddress> addresses = new ArrayList<>();

    public PractitionerEntity build(){
        PractitionerEntity practitionerEntity = new PractitionerEntity();
        practitionerEntity.setActive(true);
        practitionerEntity.setId(DEFAULT_ID);
        practitionerEntity.setNames(names);
        practitionerEntity.setAddresseses(addresses);
        return practitionerEntity;
    }

    public PractitionerEntityBuilder addAddress(String line1, String line2, String line3, String city, String district, String postcode) {
        PractitionerAddress address = new PractitionerAddress();
        address.setAddressType(Address.AddressType.BOTH);
        address.setAddressUse(Address.AddressUse.WORK);
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setAddress1(line1);
        addressEntity.setAddress2(line2);
        addressEntity.setAddress3(line3);
        addressEntity.setCity(city);
        addressEntity.setCounty(district);
        addressEntity.setPostcode(postcode);
        address.setAddress(addressEntity);
        addresses.add(address);
        return this;
    }

    public PractitionerEntityBuilder addName(String prefix, String givenName, String familyName){
        PractitionerName name = new PractitionerName();
        name.setFamilyName(familyName);
        name.setGivenName(givenName);
        name.setPrefix(prefix);
        name.setNameUse(HumanName.NameUse.USUAL);
        names.add(name);
        return this;
    }

}
