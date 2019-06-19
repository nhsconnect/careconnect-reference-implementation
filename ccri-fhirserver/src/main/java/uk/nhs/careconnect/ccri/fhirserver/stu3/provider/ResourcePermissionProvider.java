package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;


@Component
public class ResourcePermissionProvider {



	private static final Logger log = LoggerFactory.getLogger(ResourcePermissionProvider.class);


	public void checkPermission(String operation) {

    	log.trace("Check "+operation);
        if(!HapiProperties.getServerCrudRead() && operation.equals("read"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new NotImplementedOperationException("Not implemented GET"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
		
		if(!HapiProperties.getServerCrudUpdate() && operation.equals("update"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new NotImplementedOperationException("Not implemented PUT"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
		
		if(!HapiProperties.getServerCrudDelete() && operation.equals("delete"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new NotImplementedOperationException("Not implemented DEL"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
		
		if(!HapiProperties.getServerCrudCreate() && operation.equals("create"))
		{
			throw OperationOutcomeFactory.buildOperationOutcomeException(
			new NotImplementedOperationException("Not implemented POST"),
			OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID);
		}
              		
    }

}
