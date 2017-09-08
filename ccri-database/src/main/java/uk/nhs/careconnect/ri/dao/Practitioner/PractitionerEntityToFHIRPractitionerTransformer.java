package uk.nhs.careconnect.ri.dao.Practitioner;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Enumerations;
import org.hl7.fhir.instance.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

@Component
public class PractitionerEntityToFHIRPractitionerTransformer implements Transformer<PractitionerEntity, Practitioner> {

    @Override
    public Practitioner transform(final PractitionerEntity practitionerEntity) {
        final Practitioner practitioner = new Practitioner();

        

        for(int f=0;f<practitionerEntity.getIdentifiers().size();f++)
        {
            practitioner.addIdentifier()
                    .setSystem(practitionerEntity.getIdentifiers().get(f).getSystem().getUri())
                    .setValue(practitionerEntity.getIdentifiers().get(f).getValue());
        }


        practitioner.setId(practitionerEntity.getId().toString());

        practitioner.getName()
                .addFamily(practitionerEntity.getFamilyName())
                .addGiven(practitionerEntity.getGivenName())
                .addPrefix(practitionerEntity.getPrefix());
                


        for(int f=0;f<practitionerEntity.getAddresses().size();f++)
        {
            AddressEntity adressEnt = practitionerEntity.getAddresses().get(f).getAddress();

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
            practitioner.addAddress(adr);
        }
        if (practitionerEntity.getGender() !=null)
        {
            switch (practitionerEntity.getGender())
            {
                case "MALE":
                    practitioner.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "FEMALE":
                    practitioner.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                case "OTHER":
                    practitioner.setGender(Enumerations.AdministrativeGender.OTHER);
                    break;
                case "UNKNOWN":
                    practitioner.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                    break;
            }
        }
        return practitioner;

    }
}
