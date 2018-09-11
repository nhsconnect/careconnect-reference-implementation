package uk.nhs.careconnect.ri.database.daointerface.transforms.builder;

import org.hl7.fhir.dstu3.model.HumanName;
import uk.nhs.careconnect.ri.database.daointerface.transforms.DateUtils;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientName;

import java.time.LocalDate;
import java.util.Date;

public class PatientEntityBuilder {

    private Long patientId = 100002L;
    private boolean active = true;
    private LocalDate dateOfBirth = LocalDate.of(1990, 1,1);

    public PatientEntityBuilder setDateOfBirth(Date dateOfBirth) {
        return setDateOfBirth(DateUtils.asLocalDate(dateOfBirth));
    }

    public PatientEntityBuilder setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public PatientEntity build() {
        final PatientEntity patientEntity = new PatientEntity();
        patientEntity.setId(patientId);
        patientEntity.setActive(active);
        patientEntity.setDateOfBirth(DateUtils.asDate(dateOfBirth));
        patientEntity.setGender("MALE");
        final PatientName name = patientEntity.addName();
        name.setNameUse(HumanName.NameUse.OFFICIAL);
        name.setGivenName("John");
        name.setFamilyName("Smith");
        name.setPrefix("Mr");
        // KGM 18/12/2017 Removed following line. Add name does this functionality
       // patientEntity.getNames().add(name);
        return patientEntity;
    }
}
