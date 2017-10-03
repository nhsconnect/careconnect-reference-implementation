package uk.nhs.careconnect.ri.daointerface.Transforms;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Test;
import uk.nhs.careconnect.ri.daointerface.Transforms.builder.PatientEntityBuilder;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.equalTo;

public class PatientEntityToFHIRPatientTransformerTest {

    PatientEntityToFHIRPatientTransformer transformer = new PatientEntityToFHIRPatientTransformer();

    @Test
    public void testTransformSimplePatientEntity() throws FHIRException {

        LocalDate dateOfBirth = LocalDate.of(1996, 6, 21);

        final PatientEntity patientEntity = new PatientEntityBuilder()
                .setDateOfBirth(dateOfBirth)
                .build();

        final Patient patient = transformer.transform(patientEntity);

        assertThat(patient, not(nullValue()));
        assertThat(patient.getActive(), equalTo(true));
        assertThat(patient.getName().size(), equalTo(1));

        HumanName name = patient.getName().get(0);
        assertThat(name.getGivenAsSingleString(), equalTo("John"));
        assertThat(name.getFamily(), equalTo("Smith"));
        assertThat(name.getUse(), equalTo(HumanName.NameUse.USUAL));
        assertThat(name.getPrefixAsSingleString(), equalTo("Mr"));

        assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));

        assertThat(patient.getBirthDate(), not(nullValue()));
        LocalDate patientDoB = DateUtils.asLocalDate(patient.getBirthDate());
        assertThat(patientDoB, equalTo(dateOfBirth));

    }

}