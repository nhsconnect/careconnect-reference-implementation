package uk.nhs.careconnect.ri.dao.Patient;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Enumerations;
import org.hl7.fhir.instance.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

@Component
public class PatientEntityToFHIRPatientTransformer implements Transformer<PatientEntity, Patient> {

    @Override
    public Patient transform(final PatientEntity patientEntity) {
        final Patient patient = new Patient();

        

        for(int f=0;f<patientEntity.getIdentifiers().size();f++)
        {
            patient.addIdentifier()
                    .setSystem(patientEntity.getIdentifiers().get(f).getSystem().getUri())
                    .setValue(patientEntity.getIdentifiers().get(f).getValue());
        }


        patient.setId(patientEntity.getId().toString());

        patient.addName()
                .addFamily(patientEntity.getFamilyName())
                .addGiven(patientEntity.getGivenName())
                .addPrefix(patientEntity.getPrefix());

        if (patientEntity.getDateOfBirth() != null)
        {
            patient.setBirthDate(patientEntity.getDateOfBirth());
        }

        for(int f=0;f<patientEntity.getAddresses().size();f++)
        {
            AddressEntity adressEnt = patientEntity.getAddresses().get(f).getAddress();

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
            patient.addAddress(adr);
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
        return patient;

    }
}
