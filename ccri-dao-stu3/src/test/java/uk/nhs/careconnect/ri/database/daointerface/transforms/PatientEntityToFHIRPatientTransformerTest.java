package uk.nhs.careconnect.ri.database.daointerface.transforms;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.careconnect.ri.dao.transforms.BaseAddressToFHIRAddressTransformer;
import uk.nhs.careconnect.ri.dao.transforms.PatientEntityToFHIRPatientTransformer;
import uk.nhs.careconnect.ri.database.daointerface.transforms.builder.PatientEntityBuilder;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PatientEntityToFHIRPatientTransformerTest {

    private PatientEntityToFHIRPatientTransformer transformer;

    @Before
    public void setup(){
        BaseAddressToFHIRAddressTransformer addressTransformer = new BaseAddressToFHIRAddressTransformer();
        transformer = new PatientEntityToFHIRPatientTransformer(addressTransformer);
    }

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
        assertThat(name.getUse(), equalTo(HumanName.NameUse.OFFICIAL));
        assertThat(name.getPrefixAsSingleString(), equalTo("Mr"));

        assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));

        assertThat(patient.getBirthDate(), not(nullValue()));
        LocalDate patientDoB = DateUtils.asLocalDate(patient.getBirthDate());
        assertThat(patientDoB, equalTo(dateOfBirth));
    }

}