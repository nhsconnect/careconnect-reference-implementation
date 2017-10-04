package uk.nhs.careconnect.ri.daointerface.Transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.BaseAddress;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerAddress;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.org.hl7.fhir.core.Dstu2.CareConnectProfile;

@Component
public class PractitionerEntityToFHIRPractitionerTransformer implements Transformer<PractitionerEntity, Practitioner> {

    private final Transformer<BaseAddress, Address> addressTransformer;

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
        /* TODO STU3 move to new resource
        for (PractitionerRole role  : practitionerEntity.getRoles()) {
            Practitioner.PractitionerPractitionerRoleComponent practitionerRole = practitioner.addPractitionerRole();
            if (role.getManaginsOrganisation() != null) {
                practitionerRole.getManagingOrganization()
                        .setReference("Organization/"+role.getManaginsOrganisation().getId())
                        .setDisplay(role.getManaginsOrganisation().getName());
            }
            if (role.getRole() != null) {
                practitionerRole.getRole().addCoding()
                        .setCode(role.getRole().getCode())
                        .setDisplay(role.getRole().getDisplay())
                        .setSystem(role.getRole().getSystem());
            }
            for (ConceptEntity specialty : role.getSpecialties()) {
                practitionerRole.addSpecialty().addCoding()
                        .setCode(specialty.getCode())
                        .setDisplay(specialty.getDisplay())
                        .setSystem(specialty.getSystem());
            }
        }
        */
        return practitioner;

    }
}
