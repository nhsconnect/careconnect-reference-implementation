package uk.nhs.careconnect.ri.entity.patient;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.model.patient.PatientDetails;

@Component
public class PatientDetailsToEntityTransformer implements Transformer<PatientDetails, PatientEntity> {


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
