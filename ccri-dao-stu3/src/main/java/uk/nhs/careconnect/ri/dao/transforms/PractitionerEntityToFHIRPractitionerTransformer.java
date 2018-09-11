package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerAddress;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class PractitionerEntityToFHIRPractitionerTransformer implements Transformer<PractitionerEntity, Practitioner> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PractitionerEntityToFHIRPractitionerTransformer.class);


    public PractitionerEntityToFHIRPractitionerTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public Practitioner transform(final PractitionerEntity practitionerEntity) {
        final Practitioner practitioner = new Practitioner();

        Meta meta = new Meta().addProfile(CareConnectProfile.Practitioner_1);

        if (practitionerEntity.getUpdated() != null) {
            meta.setLastUpdated(practitionerEntity.getUpdated());
        }
        else {
            if (practitionerEntity.getCreated() != null) {
                meta.setLastUpdated(practitionerEntity.getCreated());
            }
        }
        practitioner.setMeta(meta);
        if (practitionerEntity.getActive() != null) {
            practitioner.setActive(practitionerEntity.getActive());
        }

        for(int f=0;f<practitionerEntity.getIdentifiers().size();f++)
        {
            practitioner.addIdentifier()
                    .setSystem(practitionerEntity.getIdentifiers().get(f).getSystem().getUri())
                    .setValue(practitionerEntity.getIdentifiers().get(f).getValue());
        }


        practitioner.setId(practitionerEntity.getId().toString());

        if (practitionerEntity.getNames().size() > 0) {

            practitioner.addName()
                    .setFamily(practitionerEntity.getNames().get(0).getFamilyName())
                    .addGiven(practitionerEntity.getNames().get(0).getGivenName())
                    .addPrefix(practitionerEntity.getNames().get(0).getPrefix());
        }
        for(int f=0;f<practitionerEntity.getTelecoms().size();f++)
        {
            practitioner.addTelecom()
                    .setSystem(practitionerEntity.getTelecoms().get(f).getSystem())
                    .setValue(practitionerEntity.getTelecoms().get(f).getValue())
                    .setUse(practitionerEntity.getTelecoms().get(f).getTelecomUse());
        }


        for (PractitionerAddress practitionerAddress : practitionerEntity.getAddresses()){
            Address address = addressTransformer.transform(practitionerAddress);
            practitioner.addAddress(address);
        }

        if (practitionerEntity.getGender() !=null)
        {
            practitioner.setGender(daoutils.getGender(practitionerEntity.getGender()));
        }

        return practitioner;

    }
}
