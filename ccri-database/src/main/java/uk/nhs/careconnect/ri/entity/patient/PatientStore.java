package uk.nhs.careconnect.ri.entity.patient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.careconnect.ri.model.patient.PatientDetails;

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
