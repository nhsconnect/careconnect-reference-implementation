package uk.nhs.careconnect.ri.dao.Organisation;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.ContactPoint;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Organization;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;

@Component
public class OrganisationEntityToFHIROrganizationTransformer implements Transformer<OrganisationEntity, Organization> {

    @Override
    public Organization transform(final OrganisationEntity organisationEntity) {
        final Organization organisation = new Organization();

        Meta meta = new Meta().addProfile(CareConnectProfile.Organization_1);

        if (organisationEntity.getUpdated() != null) {
            meta.setLastUpdated(organisationEntity.getUpdated());
        }
        else {
            if (organisationEntity.getCreated() != null) {
                meta.setLastUpdated(organisationEntity.getCreated());
            }
        }
        organisation.setMeta(meta);

        for(int f=0;f<organisationEntity.getIdentifiers().size();f++)
        {
            organisation.addIdentifier()
                    .setSystem(organisationEntity.getIdentifiers().get(f).getSystem().getUri())
                    .setValue(organisationEntity.getIdentifiers().get(f).getValue());
        }


        organisation.setId(organisationEntity.getId().toString());

        organisation.setName(
                organisationEntity.getName());


        for(int f=0;f<organisationEntity.getTelecoms().size();f++)
        {
            organisation.addContact()
                    .addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(organisationEntity.getTelecoms().get(f).getValue())
                    .setUse(organisationEntity.getTelecoms().get(f).getTelecomUse());
        }


        for(int f=0;f<organisationEntity.getAddresses().size();f++)
        {
            AddressEntity adressEnt = organisationEntity.getAddresses().get(f).getAddress();

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
            if (organisationEntity.getAddresses().get(f).getAddressType() != null) {
                adr.setType(organisationEntity.getAddresses().get(f).getAddressType());
            }
            if (organisationEntity.getAddresses().get(f).getAddressUse() != null) {
                adr.setUse(organisationEntity.getAddresses().get(f).getAddressUse());
            }
            organisation.addAddress(adr);
        }

        return organisation;

    }
}
