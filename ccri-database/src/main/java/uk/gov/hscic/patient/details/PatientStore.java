package uk.gov.hscic.patient.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.patient.PatientDetails;

@Service
public class PatientStore {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private PatientDetailsToEntityTransformer transformer;

	public void create(PatientDetails patientDetails) {
		PatientEntity patientEntity = transformer.transform(patientDetails);

		patientRepository.saveAndFlush(patientEntity);
	}
}
