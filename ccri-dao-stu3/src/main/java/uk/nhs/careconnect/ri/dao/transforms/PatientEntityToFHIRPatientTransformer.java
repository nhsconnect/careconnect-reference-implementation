package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.patient.*;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerName;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.patient.*;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;


@Component
public class PatientEntityToFHIRPatientTransformer implements Transformer<PatientEntity, Patient> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PatientEntityToFHIRPatientTransformer.class);


    public PatientEntityToFHIRPatientTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public Patient transform(final PatientEntity patientEntity) {
        final Patient patient = new Patient();

        Meta meta = new Meta().addProfile(CareConnectProfile.Patient_1);

        if (patientEntity.getUpdated() != null) {
            meta.setLastUpdated(patientEntity.getUpdated());
        }
        else {
            if (patientEntity.getCreated() != null) {
                meta.setLastUpdated(patientEntity.getCreated());
            }
        }
        patient.setMeta(meta);


        for(PatientIdentifier patientIdentifier : patientEntity.getIdentifiers())
        {
            Identifier identifier = patient.addIdentifier()
                    .setSystem(patientIdentifier.getSystemUri())
                    .setValue(patientIdentifier.getValue());
                    //.setUse(Identifier.IdentifierUse.SECONDARY);

            if (identifier.getSystem().equals("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier")) {
                //identifier.setUse(Identifier.IdentifierUse.OFFICIAL);
            }
           // NHS Verification Status
            if ( (patientIdentifier.getSystemUri().equals(CareConnectSystem.NHSNumber))
                    && (patientEntity.getNHSVerificationCode() != null)) {
                //identifier.setUse(Identifier.IdentifierUse.OFFICIAL);
                CodeableConcept verificationStatusCode = new CodeableConcept();
                verificationStatusCode
                        .addCoding()
                        .setSystem(CareConnectSystem.NHSNumberVerificationStatus)
                        .setDisplay(patientEntity.getNHSVerificationCode().getDisplay())
                        .setCode(patientEntity.getNHSVerificationCode().getCode());
                Extension verificationStatus = new Extension()
                        .setUrl(CareConnectExtension.UrlNHSNumberVerificationStatus)
                        .setValue(verificationStatusCode);
                identifier.addExtension(verificationStatus);
            }

        }


        patient.setId(patientEntity.getId().toString());

        Boolean officialFound = false;
        for (PatientName nameEntity : patientEntity.getNames()) {

            HumanName name = patient.addName()
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
        if (!officialFound && patient.getName().size()>0) {
            // No official name found. It is required so as a workaround make the first one official THIS IS A WORK AROUND and not to be implementated live
            patient.getName().get(0).setUse(HumanName.NameUse.OFFICIAL);
        }
        if (patientEntity.getDateOfBirth() != null)
        {
            patient.setBirthDate(patientEntity.getDateOfBirth());
        }

        for (PatientAddress patientAddress : patientEntity.getAddresses()){
            Address address = addressTransformer.transform(patientAddress);
            patient.addAddress(address);
        }

        for(PatientTelecom telecom : patientEntity.getTelecoms())
        {
            patient.addTelecom()
                    .setSystem(telecom.getSystem())
                    .setValue(telecom.getValue())
                    .setUse(telecom.getTelecomUse());
        }


        if (patientEntity.getActiveRecord() != null) {
            patient.setActive(patientEntity.getActiveRecord());
        }


        if (patientEntity.getGender() !=null) {
           patient.setGender(daoutils.getGender((patientEntity.getGender())));
        }

        if (patientEntity.getGP() != null && patientEntity.getGP().getNames().size() > 0) {
            PractitionerName gpName = patientEntity.getGP().getNames().get(0);
            patient.addGeneralPractitioner()
                    .setDisplay(gpName.getDisplayName())
                    .setReference("Practitioner/"+patientEntity.getGP().getId());

        }

        if (patientEntity.getEthnicCode() !=null) {
            CodeableConcept ethnicCode = new CodeableConcept();
            ethnicCode
                    .addCoding()
                    .setSystem(patientEntity.getEthnicCode().getSystem())
                    .setDisplay(patientEntity.getEthnicCode().getDisplay())
                    .setCode(patientEntity.getEthnicCode().getCode());
            Extension ethnicExtension = new Extension()
                    .setUrl(CareConnectExtension.UrlEthnicCategory)
                    .setValue(ethnicCode);
            patient.addExtension(ethnicExtension);
        }

        if (patientEntity.getMaritalCode() != null) {

            CodeableConcept marital = new CodeableConcept();
            marital.addCoding()
                    .setSystem(patientEntity.getMaritalCode().getSystem())
                    .setCode(patientEntity.getMaritalCode().getCode())
                    .setDisplay(patientEntity.getMaritalCode().getDisplay());
            patient.setMaritalStatus(marital);
        }


        if (patientEntity.getPractice()!=null) {
            patient.setManagingOrganization(new Reference("Organization/"+patientEntity.getPractice().getId()));
            patient.getManagingOrganization().setDisplay(patientEntity.getPractice().getName());
        }

        return patient;

    }
}
