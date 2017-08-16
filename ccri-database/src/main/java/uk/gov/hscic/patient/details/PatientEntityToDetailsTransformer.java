package uk.gov.hscic.patient.details;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hscic.medical.practicitioners.doctor.GPEntity;
import uk.gov.hscic.model.patient.PatientDetails;

@Component
public class PatientEntityToDetailsTransformer implements Transformer<PatientEntity, PatientDetails> {

    @Override
    public PatientDetails transform(final PatientEntity patientEntity) {
        final PatientDetails patient = new PatientDetails();

        Collection<String> addressList = Arrays.asList(StringUtils.trimToNull(patientEntity.getAddress1()),
                                                       StringUtils.trimToNull(patientEntity.getAddress2()),
                                                       StringUtils.trimToNull(patientEntity.getAddress3()),
                                                       StringUtils.trimToNull(patientEntity.getPostcode()));

        addressList = CollectionUtils.removeAll(addressList, Collections.singletonList(null));

        final String address = StringUtils.join(addressList, ", ");
        final String name = patientEntity.getFirstName() + " " + patientEntity.getLastName();
        final String patientId = patientEntity.getNhsNumber();

        patient.setId(String.valueOf(patientEntity.getId()));
        patient.setName(name);
        patient.setTitle(patientEntity.getTitle());
        patient.setForename(patientEntity.getFirstName());
        patient.setSurname(patientEntity.getLastName());
        patient.setGender(patientEntity.getGender());
        patient.setDateOfBirth(patientEntity.getDateOfBirth());
        patient.setNhsNumber(patientId);
        patient.setPasNumber(patientEntity.getPasNumber());
        patient.setAddress(address);
        patient.setTelephone(patientEntity.getPhone());
        patient.setPasNumber(patientEntity.getPasNumber());
        patient.setLastUpdated(patientEntity.getLastUpdated());
        patient.setRegistrationStartDateTime(patientEntity.getRegistrationStartDateTime());
        patient.setRegistrationEndDateTime(patientEntity.getRegistrationEndDateTime());
        patient.setRegistrationStatus(patientEntity.getRegistrationStatus());
        patient.setRegistrationType(patientEntity.getRegistrationType());

        GPEntity gp = patientEntity.getGp();
        
        if (gp != null) {
        	patient.setGpDetails(gp.getName());
        	patient.setGpId(gp.getId());
        }

        return patient;
    }
}
