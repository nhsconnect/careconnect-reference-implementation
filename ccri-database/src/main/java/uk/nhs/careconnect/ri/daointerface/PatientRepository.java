package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import java.util.List;


public interface PatientRepository {

    void save(PatientEntity patient);

    Patient read(IdType theId);

    PatientEntity readEntity(IdType theId);

    List<Patient> searchPatient (
            @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
            @OptionalParam(name= Patient.SP_BIRTHDATE) DateParam birthDate,
            @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
            @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
            @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
            @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
            @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name= Patient.SP_NAME) StringParam name,
            @OptionalParam(name= Patient.SP_PHONE) StringParam phone);

}
