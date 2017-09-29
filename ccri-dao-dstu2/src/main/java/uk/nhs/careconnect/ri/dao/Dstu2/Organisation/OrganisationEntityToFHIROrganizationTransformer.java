package uk.nhs.careconnect.ri.dao.Dstu2.Organisation;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.organization.OrganisationAddress;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationTelecom;
import uk.org.hl7.fhir.core.Dstu2.CareConnectProfile;

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


        for(OrganisationTelecom telecom : organisationEntity.getTelecoms())
        {
            organisation.addTelecom()
                    .setSystem(telecom.getSystemDstu2())
                    .setValue(telecom.getValue())
                    .setUse(telecom.getTelecomUseDstu2());
        }


        for(OrganisationAddress adressEnt : organisationEntity.getAddresses())
        {


            Address adr= new Address();
            if (adressEnt.getAddress().getAddress1()!="")
            {
                adr.addLine(adressEnt.getAddress().getAddress1());
            }
            if (adressEnt.getAddress().getAddress2()!="")
            {
                adr.addLine(adressEnt.getAddress().getAddress2());
            }
            if (adressEnt.getAddress().getAddress3()!="")
            {
                adr.addLine(adressEnt.getAddress().getAddress3());
            }
            if (adressEnt.getAddress().getAddress4()!="")
            {
                adr.addLine(adressEnt.getAddress().getAddress4());
            }
            if (adressEnt.getAddress().getPostcode() !=null)
            {
                adr.setPostalCode(adressEnt.getAddress().getPostcode());
            }
            if (adressEnt.getAddress().getCity() != null) {
                adr.setCity(adressEnt.getAddress().getCity());
            }
            if (adressEnt.getAddress().getCounty() != null) {
                adr.setDistrict(adressEnt.getAddress().getCounty());
            }
            if (adressEnt.getAddressType() != null) {
                adr.setType(adressEnt.getAddressTypeDstu2());
            }
            if (adressEnt.getAddressUse() != null) {
                adr.setUse(adressEnt.getAddressUseDstu2());
            }
            organisation.addAddress(adr);
        }
        if (organisationEntity.getType()!=null) {
            organisation.getType().addCoding()
                    .setCode(organisationEntity.getType().getCode())
                    .setDisplay(organisationEntity.getType().getDisplay())
                    .setSystem(organisationEntity.getType().getSystem());
        }
        if (organisationEntity.getPartOf() != null) {
            organisation.setPartOf(new Reference("Organization/"+organisationEntity.getPartOf().getId()));
            organisation.getPartOf().setDisplay(organisationEntity.getPartOf().getName());
        }
        if (organisationEntity.getActive() != null) {
            organisation.setActive(organisationEntity.getActive());
        }
        return organisation;

    }
}
