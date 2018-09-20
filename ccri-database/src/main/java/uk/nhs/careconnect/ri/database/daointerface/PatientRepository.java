package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import java.util.List;
import java.util.Set;


public interface PatientRepository extends BaseRepository<PatientEntity,Patient> {

    void save(FhirContext ctx, PatientEntity patient) throws OperationOutcomeException;

    Patient read(FhirContext ctx, IdType theId);

    PatientEntity readEntity(FhirContext ctx,IdType theId);

    Patient update(FhirContext ctx, Patient patient, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Resource> search (FhirContext ctx,
                           @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                           @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
                           @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                           @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                           @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                           @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                           @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                           @OptionalParam(name= Patient.SP_NAME) StringParam name,
                           @OptionalParam(name= Patient.SP_PHONE) StringParam phone
            , @OptionalParam(name= Patient.SP_RES_ID) StringParam id
            , @IncludeParam(reverse=true, allow = {"*"}) Set<Include> reverseIncludes
            , @IncludeParam(allow= {
                    "Patient:general-practitioner"
                    ,"Patient:organization"
                    , "*"}) Set<Include> includes);

    List<PatientEntity> searchEntity (FhirContext ctx,
                          @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                          @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
                          @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                          @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                          @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                          @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                          @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                          @OptionalParam(name= Patient.SP_NAME) StringParam name,
                          @OptionalParam(name= Patient.SP_PHONE) StringParam phone
            ,@OptionalParam(name= Patient.SP_RES_ID) StringParam id
            ,@IncludeParam(reverse=true, allow = {"*"}) Set<Include> reverseIncludes
            ,@IncludeParam(allow= {
                "Patient:general-practitioner"
                ,"Patient:organization"
                , "*"}) Set<Include> includes);

}
