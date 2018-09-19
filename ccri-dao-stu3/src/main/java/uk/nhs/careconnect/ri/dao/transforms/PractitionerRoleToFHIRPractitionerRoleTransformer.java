package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Meta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRole;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRoleIdentifier;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerSpecialty;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;


@Component
public class PractitionerRoleToFHIRPractitionerRoleTransformer implements Transformer<PractitionerRole, org.hl7.fhir.dstu3.model.PractitionerRole> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PractitionerRoleToFHIRPractitionerRoleTransformer.class);


    public PractitionerRoleToFHIRPractitionerRoleTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole transform(final PractitionerRole roleEntity) {
        final org.hl7.fhir.dstu3.model.PractitionerRole practitionerRole = new org.hl7.fhir.dstu3.model.PractitionerRole();

        Meta meta = new Meta().addProfile(CareConnectProfile.PractitionerRole_1);

        if (roleEntity.getUpdated() != null) {
            meta.setLastUpdated(roleEntity.getUpdated());
        }
        else {
            if (roleEntity.getCreated() != null) {
                meta.setLastUpdated(roleEntity.getCreated());
            }
        }

        practitionerRole.setId(roleEntity.getId().toString());

        for(PractitionerRoleIdentifier identifier : roleEntity.getIdentifiers())
        {
            practitionerRole.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }


        if (roleEntity.getOrganisation() != null) {
            practitionerRole.getOrganization()
                    .setReference("Organization/"+roleEntity.getOrganisation().getId())
                    .setDisplay(roleEntity.getOrganisation().getName());
        }
        if (roleEntity.getRole() != null) {
            CodeableConcept role = new CodeableConcept();
            role.addCoding()
                    .setCode(roleEntity.getRole().getCode())
                    .setDisplay(roleEntity.getRole().getDisplay())
                    .setSystem(roleEntity.getRole().getSystem());
            practitionerRole.getCode().add(role);
        }
        if (roleEntity.getPractitioner() != null) {
            practitionerRole.getPractitioner()
                    .setReference("Practitioner/"+roleEntity.getPractitioner().getId())
                    .setDisplay(roleEntity.getPractitioner().getNames().get(0).getDisplayName());
        }
        for (PractitionerSpecialty specialty : roleEntity.getSpecialties()) {
            CodeableConcept concept = new CodeableConcept();
            concept.addCoding()
                    .setCode(specialty.getSpecialty().getCode())
                    .setDisplay(specialty.getSpecialty().getDisplay())
                    .setSystem(specialty.getSpecialty().getSystem());
            practitionerRole.addSpecialty(concept);
        }



        return practitionerRole;

    }
}
