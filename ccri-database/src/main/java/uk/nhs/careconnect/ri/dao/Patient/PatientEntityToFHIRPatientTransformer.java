package uk.nhs.careconnect.ri.dao.Patient;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.org.hl7.fhir.core.dstu2.CareConnectExtension;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;


@Component
public class PatientEntityToFHIRPatientTransformer implements Transformer<PatientEntity, Patient> {

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


        for(int f=0;f<patientEntity.getIdentifiers().size();f++)
        {
            Identifier identifier = patient.addIdentifier()
                    .setSystem(patientEntity.getIdentifiers().get(f).getSystem().getUri())
                    .setValue(patientEntity.getIdentifiers().get(f).getValue());
           // NHS Verification Status
            if ( (patientEntity.getIdentifiers().get(f).getSystem().getUri().equals(CareConnectSystem.NHSNumber))
                    && (patientEntity.getNHSVerificationCode() != null)) {
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

        HumanName name = patient.addName()
                .addFamily(patientEntity.getFamilyName())
                .addGiven(patientEntity.getGivenName())
                .addPrefix(patientEntity.getPrefix());
        if (patientEntity.getNameUse() != null) {
            name.setUse(patientEntity.getNameUse());
        }
        if (patientEntity.getDateOfBirth() != null)
        {
            patient.setBirthDate(patientEntity.getDateOfBirth());
        }

        for(int f=0;f<patientEntity.getAddresses().size();f++)
        {
            AddressEntity adressEnt = patientEntity.getAddresses().get(f).getAddress();

            Address adr= new Address();

            adr.setUse(Address.AddressUse.HOME);
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
            if (patientEntity.getAddresses().get(f).getAddressType() != null) {
                adr.setType(patientEntity.getAddresses().get(f).getAddressType());
            }
            if (patientEntity.getAddresses().get(f).getAddressUse() != null) {
                adr.setUse(patientEntity.getAddresses().get(f).getAddressUse());
            }
            patient.addAddress(adr);
        }

        for(int f=0;f<patientEntity.getTelecoms().size();f++)
        {
            patient.addTelecom()
                    .setSystem(patientEntity.getTelecoms().get(f).getSystem())
                    .setValue(patientEntity.getTelecoms().get(f).getValue())
                    .setUse(patientEntity.getTelecoms().get(f).getTelecomUse());
        }


        if (patientEntity.getActiveRecord() != null) {
            patient.setActive(patientEntity.getActiveRecord());
        }


        if (patientEntity.getGender() !=null)
        {
            switch (patientEntity.getGender())
            {
                case "MALE":
                    patient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "FEMALE":
                    patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                case "OTHER":
                    patient.setGender(Enumerations.AdministrativeGender.OTHER);
                    break;
                case "UNKNOWN":
                    patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                    break;
            }
        }
        if (patientEntity.getGP() != null) {
            patient.addCareProvider()
                    .setDisplay(patientEntity.getGP().getPrefix()+" "+patientEntity.getGP().getGivenName()+" "+patientEntity.getGP().getFamilyName())
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
