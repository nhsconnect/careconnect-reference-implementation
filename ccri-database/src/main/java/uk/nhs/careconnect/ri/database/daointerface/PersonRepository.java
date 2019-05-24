package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Person;
import org.hl7.fhir.dstu3.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.Person.PersonEntity;

import java.util.List;


public interface PersonRepository extends BaseRepository<PersonEntity, Person> {

    void save(FhirContext ctx, PersonEntity person) throws OperationOutcomeException;

    Person read(FhirContext ctx, IdType theId);

    PersonEntity readEntity(FhirContext ctx, IdType theId);

    Person update(FhirContext ctx, Person person, @IdParam IdType theId) throws OperationOutcomeException;

    List<Resource> search (FhirContext ctx,
                           @OptionalParam(name = Person.SP_NAME) StringParam name,
                           @OptionalParam(name = Person.SP_IDENTIFIER) TokenParam identifier,
                           @OptionalParam(name = Person.SP_EMAIL) TokenParam email,
                           @OptionalParam(name = Person.SP_PHONE) TokenParam phone
    );

    List<PersonEntity> searchEntity (FhirContext ctx,
                                     @OptionalParam(name = Person.SP_NAME) StringParam name,
                                     @OptionalParam(name = Person.SP_IDENTIFIER) TokenParam identifier,
                                     @OptionalParam(name = Person.SP_EMAIL) TokenParam email,
                                     @OptionalParam(name = Person.SP_PHONE) TokenParam phone
    );

}
