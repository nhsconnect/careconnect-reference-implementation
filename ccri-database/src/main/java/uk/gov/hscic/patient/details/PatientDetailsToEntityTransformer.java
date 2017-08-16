package uk.gov.hscic.patient.details;

import java.util.List;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.medical.practicitioners.doctor.GPEntity;
import uk.gov.hscic.medical.practicitioners.doctor.GPRepository;
import uk.gov.hscic.model.patient.PatientDetails;

@Component
public class PatientDetailsToEntityTransformer implements Transformer<PatientDetails, PatientEntity> {

	@Autowired
	private GPRepository gPRepository;

	@Override
	public PatientEntity transform(PatientDetails patientDetails) {
		PatientEntity patientEntity = null;

		if (patientDetails != null) {
			patientEntity = new PatientEntity();
			String address = patientDetails.getAddress();

			if (address != null) {
				final String[] addressLines = StringUtils.split(address, ", ");

				if (addressLines.length > 0) {
					patientEntity.setAddress1(addressLines[0]);

					if (addressLines.length > 1) {
						patientEntity.setAddress2(addressLines[1]);

						if (addressLines.length > 2) {
							patientEntity.setAddress3(addressLines[2]);

							if (addressLines.length > 3) {
								patientEntity.setPostcode(addressLines[3]);
							}
						}
					}
				}
			}

			if (patientDetails.getGpDetails() != null) {
				List<GPEntity> gpList = gPRepository.findByName(patientDetails.getGpDetails());

				if (gpList.size() == 1) {
					patientEntity.setGp(gpList.get(0));
				} else if (gpList.size() > 1) {
					throw new IllegalStateException(String.format("Invalid number of GPs associated with patient - (first name - %s, last name - %s). Expected - 1. Actual - %d", patientDetails.getForename(), patientDetails.getSurname(), gpList.size()));
				}
			}

			patientEntity.setNhsNumber(patientDetails.getNhsNumber());
			patientEntity.setDateOfBirth(patientDetails.getDateOfBirth());
			patientEntity.setFirstName(patientDetails.getForename());
			patientEntity.setGender(patientDetails.getGender());
			patientEntity.setId(patientDetails.getId() != null ? Long.parseLong(patientDetails.getId()) : null);
			patientEntity.setLastName(patientDetails.getSurname());
			patientEntity.setLastUpdated(patientDetails.getLastUpdated());
			patientEntity.setNhsNumber(patientDetails.getNhsNumber());
			patientEntity.setPasNumber(patientDetails.getPasNumber());
			patientEntity.setPhone(patientDetails.getTelephone());
			patientEntity.setTitle(patientDetails.getTitle());
			patientEntity.setRegistrationStartDateTime(patientDetails.getRegistrationStartDateTime());
			patientEntity.setRegistrationEndDateTime(patientDetails.getRegistrationEndDateTime());
			patientEntity.setRegistrationStatus(patientDetails.getRegistrationStatus());
			patientEntity.setRegistrationType(patientDetails.getRegistrationType());
		}

		return patientEntity;
	}
}
