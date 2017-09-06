
package uk.nhs.careconnect.ri.entity.patient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.nhs.careconnect.ri.model.patient.PatientDetails;


@Service
@Transactional
public class PatientSearch {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientEntityToDetailsTransformer patientEntityToDetailsTransformer;


    public PatientDetails findPatient(final String patientNHSNumber) {
        final PatientEntity patient = patientRepository.findByNhsNumber(patientNHSNumber);

        return patient == null
                ? null
                : patientEntityToDetailsTransformer.transform(patient);
    }

    public PatientDetails findPatientByInternalID(final String internalID) {
        final PatientEntity patient = patientRepository.findById(Long.valueOf(internalID));

        return patient == null
                ? null
                : patientEntityToDetailsTransformer.transform(patient);
    }


}
