package uk.nhs.careconnect.ri.dao.Patient;


import org.apache.commons.collections4.CollectionUtils;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.model.patient.PatientDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
public class PatientEntityToDetailsTransformer implements Transformer<PatientEntity, PatientDetails> {

    @Override
    public PatientDetails transform(final PatientEntity patientEntity) {
        final PatientDetails patient = new PatientDetails();

        if (patientEntity.getAddresses().size()>0) {


            Collection<String> addressList = Arrays.asList(StringUtils.trimToNull(patientEntity.getAddresses().get(0).getAddress().getAddress1()),
                    StringUtils.trimToNull(patientEntity.getAddresses().get(0).getAddress().getAddress2()),
                    StringUtils.trimToNull(patientEntity.getAddresses().get(0).getAddress().getAddress3()),
                    StringUtils.trimToNull(patientEntity.getAddresses().get(0).getAddress().getPostcode()));

            addressList = CollectionUtils.removeAll(addressList, Collections.singletonList(null));

            final String address = StringUtils.join(addressList, ", ");
            patient.setAddress(address);
        }

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

        patient.setTelephone(patientEntity.getPhone());
        patient.setPasNumber(patientEntity.getPasNumber());
        patient.setLastUpdated(patientEntity.getLastUpdated());
        patient.setRegistrationStartDateTime(patientEntity.getRegistrationStartDateTime());
        patient.setRegistrationEndDateTime(patientEntity.getRegistrationEndDateTime());
        patient.setRegistrationStatus(patientEntity.getRegistrationStatus());
        patient.setRegistrationType(patientEntity.getRegistrationType());

        /*
        GPEntity gp = patientEntity.getGp();
        
        if (gp != null) {
        	patient.setGpDetails(gp.getName());
        	patient.setGpId(gp.getId());
        }
        */
        return patient;
    }
}
