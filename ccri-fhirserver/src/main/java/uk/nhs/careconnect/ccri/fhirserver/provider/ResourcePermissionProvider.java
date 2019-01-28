package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;


@Component
public class ResourcePermissionProvider {

	@Value("${ccri.CRUD_read}")
	private String CRUD_read;

	@Value("${ccri.CRUD_update}")
	private String CRUD_update;
	    
	@Value("${ccri.CRUD_delete}")
	private String CRUD_delete;
	   
	@Value("${ccri.CRUD_create}")
	private String CRUD_create;
    
    public void checkPermission(String operation) {
        
        if(CRUD_read.equals("false") && operation.equals("read"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new MethodNotAllowedException("Invalid Request"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
		
		if(CRUD_update.equals("false") && operation.equals("update"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new MethodNotAllowedException("Invalid Request"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
		
		if(CRUD_delete.equals("false") && operation.equals("delete"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new MethodNotAllowedException("Invalid Request"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
		
		if(CRUD_create.equals("false") && operation.equals("create"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new MethodNotAllowedException("Invalid Request"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
              		
    }

}
