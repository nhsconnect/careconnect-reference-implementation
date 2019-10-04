package uk.nhs.careconnect.ri.stu3.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.Person.*;
import uk.nhs.careconnect.ri.stu3.dao.daoutils;


@Component
public class PersonEntityToFHIRPersonTransformer implements Transformer<PersonEntity, Person> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PersonEntityToFHIRPersonTransformer.class);


    public PersonEntityToFHIRPersonTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public Person transform(final PersonEntity personEntity) {
        final Person person = new Person();

        log.info(personEntity.getId().toString());
        person.setId(personEntity.getId().toString());

        for(PersonIdentifier personIdentifier : personEntity.getIdentifiers())
        {
            Identifier identifier = person.addIdentifier();

            identifier = daoutils.getIdentifierStrict(personIdentifier, identifier);


        }




        Boolean officialFound = false;
        for (PersonName nameEntity : personEntity.getNames()) {

            HumanName name = person.addName()
                    .setFamily(nameEntity.getFamilyName())
                    .addPrefix(nameEntity.getPrefix());

            String[] given = nameEntity.getGivenName().split(" ");
            for (Integer i=0; i<given.length; i++  ) {
                name.getGiven().add(new StringType(given[i]));
            }

            if (nameEntity.getNameUse() != null) {
                name.setUse(nameEntity.getNameUse());
                if (nameEntity.getNameUse().equals(HumanName.NameUse.OFFICIAL)) {
                    officialFound = true;
                }
            } else {
                name.setUse(HumanName.NameUse.OFFICIAL);
                officialFound = true;
            }
        }
        if (!officialFound && person.getName().size()>0) {
            // No official name found. It is required so as a workaround make the first one official THIS IS A WORK AROUND and not to be implementated live
            person.getName().get(0).setUse(HumanName.NameUse.OFFICIAL);
        }
        if (personEntity.getDateOfBirth() != null)
        {
            person.setBirthDate(personEntity.getDateOfBirth());
        }

        for (PersonAddress personAddress : personEntity.getAddresses()){
            Address address = addressTransformer.transform(personAddress);
            person.addAddress(address);
        }

        for(PersonTelecom telecom : personEntity.getTelecoms()) {
            person.addTelecom()
                    .setSystem(telecom.getSystem())
                    .setValue(telecom.getValue())
                    .setUse(telecom.getTelecomUse());
        }


        if (personEntity.getGender() !=null) {
           person.setGender(daoutils.getGender((personEntity.getGender())));
        }


        return person;

    }
}
