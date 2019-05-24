package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Person;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.PersonRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Component
public class PersonProvider implements ICCResourceProvider {
	
   
    @Autowired
    private PersonRepository personDao;

    @Autowired
    FhirContext ctx;

    @Override
    public Long count() {
        return personDao.count();
    }

    @Override
    public Class<Person> getResourceType() {
        return Person.class;
    }

    private static final Logger log = LoggerFactory.getLogger(PersonProvider.class);


    @Read
    public Person read(@IdParam IdType internalId) {

    	
        Person person = personDao.read(ctx,internalId);
        if (person == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No person details found for person ID: " + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return person;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Person person, @IdParam IdType theId, RequestDetails theRequestDetails) throws OperationOutcomeException {

        log.debug("Update Person Provider called");
        

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        Person newPerson = null;

        newPerson = personDao.update(ctx, person, theId);

        method.setId(newPerson.getIdElement());
        method.setResource(newPerson);


        log.debug("called update Person method");

        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Person person) throws OperationOutcomeException {

        log.info("Update Person Provider called");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        Person newPerson = null;

        newPerson = personDao.update(ctx, person, null);

        method.setId(newPerson.getIdElement());
        method.setResource(newPerson);

        log.debug("called create Person method");

        return method;
    }

    @Search
    public List<Resource> searchPerson(HttpServletRequest theRequest,
                                       @OptionalParam(name= Person.SP_NAME) StringParam name,
                                       @OptionalParam(name = Person.SP_IDENTIFIER) TokenParam identifier,
                                       @OptionalParam(name = Person.SP_EMAIL) TokenParam email,
                                       @OptionalParam(name = Person.SP_PHONE) TokenParam phone
     ) {
        return personDao.search(ctx,name, identifier, email, phone);
    }



}
