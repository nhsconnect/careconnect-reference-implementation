package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ValueSetProvider implements IResourceProvider {


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

    @Search
    public List<ValueSet> searchOrganisation(HttpServletRequest theRequest,
                                                 @OptionalParam(name =ValueSet.SP_NAME) StringParam name
    ) {
        return valueSetDao.searchValueset(name);
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
                    new ResourceNotFoundException("No ValueSet/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return valueSet;
    }
    

}
