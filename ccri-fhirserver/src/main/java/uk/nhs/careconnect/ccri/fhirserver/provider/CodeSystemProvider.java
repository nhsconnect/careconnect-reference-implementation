package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class CodeSystemProvider implements IResourceProvider {

	@Autowired
    private CodeSystemRepository codeSystemDao;
	
	@Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

	@Autowired
    private ResourceTestProvider resourceTestProvider;

    @Override
    public Class<CodeSystem> getResourceType() {
        return CodeSystem.class;
    }




    @Search
    public List<CodeSystem> search(HttpServletRequest theRequest,
                                                 @OptionalParam(name =CodeSystem.SP_NAME) StringParam name
    ) {
        return null;
    }




    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam CodeSystem codeSystem) {

    	resourcePermissionProvider.checkPermission("create");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

      //  codeSystem = codeSystemDao.create(codeSystem);

        return method;
    }


    @Validate
    public MethodOutcome testResource(@ResourceParam CodeSystem resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    

}
