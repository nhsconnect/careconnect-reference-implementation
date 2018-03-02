package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;

import java.util.List;


public interface IPatient {

    String findInsert(FhirContext ctx, Patient patient);

    List<Resource> search (FhirContext ctx,

                           @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,

                           @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                           @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                           @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                           @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                           @OptionalParam(name= Patient.SP_NAME) StringParam name
    );
}
