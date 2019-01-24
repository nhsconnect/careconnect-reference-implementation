package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ValueSetProvider implements IResourceProvider {


    @Autowired
    private ValueSetRepository valueSetDao;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    @Override
    public Class<ValueSet> getResourceType() {
        return ValueSet.class;
    }


    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Update()
    public MethodOutcome updateValueSet(HttpServletRequest theRequest,@ResourceParam ValueSet valueSet) {

    	resourcePermissionProvider.checkPermission("update");
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

    	resourcePermissionProvider.checkPermission("create");
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
    	resourcePermissionProvider.checkPermission("read");
        ValueSet valueSet = valueSetDao.read(internalId);

        if ( valueSet == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No ValueSet/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return valueSet;
    }
    
    @Validate
    public MethodOutcome testResource(@ResourceParam ValueSet resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
