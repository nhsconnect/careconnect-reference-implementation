package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.*;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.*;


@Component
public class RelatedPersonEntityToFHIRRelatedPersonTransformer implements Transformer<RelatedPersonEntity, RelatedPerson> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RelatedPersonEntityToFHIRRelatedPersonTransformer.class);


    public RelatedPersonEntityToFHIRRelatedPersonTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public RelatedPerson transform(final RelatedPersonEntity personEntity) {
        final RelatedPerson person = new RelatedPerson();

        Meta meta = new Meta();

        if (personEntity.getUpdated() != null) {
            meta.setLastUpdated(personEntity.getUpdated());
        }
        else {
            if (personEntity.getCreated() != null) {
                meta.setLastUpdated(personEntity.getCreated());
            }
        }
        person.setMeta(meta);

        person.setId(personEntity.getId().toString());

        for(RelatedPersonIdentifier personIdentifier : personEntity.getIdentifiers()) {
            Identifier identifier =  person.addIdentifier();

            identifier.setSystem(personIdentifier.getSystemUri()).setValue(personIdentifier.getValue());

        }


        Boolean officialFound = false;
        for (RelatedPersonName nameEntity : personEntity.getNames()) {

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

        for (RelatedPersonAddress personAddress : personEntity.getAddresses()){
            Address address = addressTransformer.transform(personAddress);
            person.addAddress(address);
        }

        for(RelatedPersonTelecom telecom : personEntity.getTelecoms())
        {
            person.addTelecom()
                    .setSystem(telecom.getSystem())
                    .setValue(telecom.getValue())
                    .setUse(telecom.getTelecomUse());
        }


        if (personEntity.getActiveRecord() != null) {
            person.setActive(personEntity.getActiveRecord());
        }


        if (personEntity.getGender() !=null) {
           person.setGender(daoutils.getGender((personEntity.getGender())));
        }

        if (personEntity.getRelationship() != null) {
            person.getRelationship()
                .addCoding()
                    .setDisplay(personEntity.getRelationship().getDisplay())
                    .setCode(personEntity.getRelationship().getCode())
                    .setSystem(personEntity.getRelationship().getSystem());
        }

        if (personEntity.getPatient() != null) {
            person.setPatient(new Reference("Patient/"+personEntity.getPatient().getId()));
        }
        
        return person;

    }
}
