package uk.nhs.careconnect.ri.dao.Organisation;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Organization;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

@Component
public class OrganisationEntityToFHIROrganizationTransformer implements Transformer<OrganisationEntity, Organization> {

    @Override
    public Organization transform(final OrganisationEntity organisationEntity) {
        final Organization organisation = new Organization();

        

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
            organisation.addIdentifier()
                    .setSystem(organisationEntity.getTelecoms().get(f).getSystem().getUri())
                    .setValue(organisationEntity.getTelecoms().get(f).getValue())
                    .setUse(organisationEntity.getTelecoms().get(f).getUse());
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
            organisation.addAddress(adr);
        }

        return organisation;

    }
}
