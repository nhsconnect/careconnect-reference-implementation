package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import java.util.List;


public interface PatientRepository extends BaseDao<PatientEntity,Patient> {

    void save(FhirContext ctx, PatientEntity patient);

    Patient read(FhirContext ctx, IdType theId);

    PatientEntity readEntity(FhirContext ctx,IdType theId);

    Patient update(FhirContext ctx, Patient patient, @IdParam IdType theId, @ConditionalUrlParam String theConditional);

    List<Patient> search (FhirContext ctx,
            @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
            @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
            @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
            @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
            @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
            @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
            @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name= Patient.SP_NAME) StringParam name,
            @OptionalParam(name= Patient.SP_PHONE) StringParam phone);

    List<PatientEntity> searchEntity (FhirContext ctx,
                          @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                          @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
                          @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                          @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                          @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                          @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                          @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                          @OptionalParam(name= Patient.SP_NAME) StringParam name,
                          @OptionalParam(name= Patient.SP_PHONE) StringParam phone);

}
