package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;

@Component
public class StructureDefinitionProvider implements IResourceProvider {

	@Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
   
	@Autowired
    private ResourceTestProvider resourceTestProvider;

    @Override
    public Class<StructureDefinition> getResourceType() {
        return StructureDefinition.class;
    }



    @Update()
    public MethodOutcome updateStructureDefinition(HttpServletRequest theRequest,@ResourceParam StructureDefinition structureDefinition) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        StructureDefinition newStructureDefinition = new StructureDefinition();
        method.setId(newStructureDefinition.getIdElement());
        method.setResource(newStructureDefinition);


        return method;
    }



    @Create
    public MethodOutcome createPatient(HttpServletRequest theRequest, @ResourceParam StructureDefinition structureDefinition) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

     structureDefinition = new StructureDefinition();

        return method;
    }

    @Read
    public StructureDefinition getStructureDefinition
            (@IdParam IdType internalId) {

        // TODO
    	resourcePermissionProvider.checkPermission("read");
      StructureDefinition structureDefinition = new StructureDefinition();

        if ( structureDefinition == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No StructureDefinition/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return structureDefinition;
    }
    

    @Validate
    public MethodOutcome testResource(@ResourceParam StructureDefinition resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}
