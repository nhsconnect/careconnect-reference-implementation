package uk.nhs.careconnect.ri.provider;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.dao.ValueSet.ValueSetDao;

import javax.servlet.http.HttpServletRequest;

@Component
public class ValueSetResourceProvider implements IResourceProvider {


    @Autowired
    private ValueSetDao valueSetDao;

    @Override
    public Class<ValueSet> getResourceType() {
        return ValueSet.class;
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
