package uk.gov.hscic.patient.details;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hscic.medical.practicitioners.doctor.GPEntity;
import uk.gov.hscic.model.patient.PatientDetails;

public class PatientEntityToDetailsTransformerTest {
    private static final String PATIENT_ID = "PATIENT";

    private PatientEntityToDetailsTransformer transformer;

    @Before
    public void setUp() throws Exception {
        transformer = new PatientEntityToDetailsTransformer();
    }

    @Test
    public void shouldRemoveEmptyLinesFromAddressString() {
        final PatientEntity patientEntity = dummyPatientEntity();

        patientEntity.setAddress1("line 1");
        patientEntity.setAddress2(null);
        patientEntity.setAddress3("line 3");
        patientEntity.setAddress5("");
        patientEntity.setAddress5("line 5");
        patientEntity.setPostcode("postcode");

        final PatientDetails patientDetails = transformer.transform(patientEntity);

        assertNotNull(patientDetails);
        assertEquals("line 1, line 3, postcode", patientDetails.getAddress());
    }

    @Test
    public void shouldReturnEmptyMedicationListWhenMedicationSearchThrowsException() {
        assertNotNull(transformer.transform(dummyPatientEntity()));
    }

    private PatientEntity dummyPatientEntity() {
        final PatientEntity patient = new PatientEntity();
        patient.setNhsNumber(PATIENT_ID);
        patient.setGp(new GPEntity());

        return patient;
    }
}
