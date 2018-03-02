package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;

import java.util.List;
import java.util.Set;

public interface IPatient {

    ObjectId findInsert(FhirContext ctx, Patient patient);

    List<Resource> search (FhirContext ctx,

                           @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,

                           @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                           @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                           @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                           @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                           @OptionalParam(name= Patient.SP_NAME) StringParam name
    );
}
