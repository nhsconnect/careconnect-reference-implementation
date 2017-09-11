package uk.nhs.careconnect.ri.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.SystemCode;
import uk.nhs.careconnect.ri.dao.ValueSet.ValueSetRepository;

import javax.servlet.http.HttpServletRequest;

@Component
public class ValueSetResourceProvider implements IResourceProvider {


    @Autowired
    private ValueSetRepository valueSetDao;

    @Override
    public Class<ValueSet> getResourceType() {
        return ValueSet.class;
    }



    @Update()
    public MethodOutcome updateValueSet(HttpServletRequest theRequest,@ResourceParam ValueSet valueSet) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        ValueSet newValueSet = valueSetDao.create(valueSet);
        method.setId(newValueSet.getIdElement());
        method.setResource(newValueSet);


        return method;
    }



    @Create
    public MethodOutcome createPatient(HttpServletRequest theRequest, @ResourceParam ValueSet valueSet) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        valueSet = valueSetDao.create(valueSet);

        return method;
    }

    @Read
    public ValueSet getValueSet
            (@IdParam IdType internalId) {
        ValueSet valueSet = valueSetDao.read(internalId);

        if ( valueSet == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No patient details found for patient ID: " + internalId.getIdPart()),
                    SystemCode.PRACTITIONER_NOT_FOUND, OperationOutcome.IssueType.NOTFOUND);
        }

        return valueSet;
    }
    

}
