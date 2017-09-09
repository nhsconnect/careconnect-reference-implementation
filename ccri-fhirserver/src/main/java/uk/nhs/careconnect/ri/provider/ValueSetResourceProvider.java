package uk.nhs.careconnect.ri.provider;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    

}
