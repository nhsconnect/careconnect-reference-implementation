package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;


import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@Component
public class ResourceTestProvider {

	@Value("${ccri.validate_flag}")
	private Boolean validate_flag;

	@Value("${ccri.validate_use_tkw}")
	private Boolean tkw_flag;


	@Value("${ccri.tkw_server}")
	private String tkw_server;
	
    @Autowired
    FhirContext ctx;

 //   @Autowired
//	FhirInstanceValidator instanceValidator;

    @Autowired
    FhirValidator val;
    
	HttpResponse response;
	Reader reader;
    private static final Logger log = LoggerFactory.getLogger(ResourceTestProvider.class);

    
    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }

    public Boolean pass(MethodOutcome methodOutcome) {

    	if (methodOutcome.getOperationOutcome() == null) return false;
    	OperationOutcome outcome = (OperationOutcome) methodOutcome.getOperationOutcome();
    	for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {
    		switch (issue.getSeverity()) {
				case ERROR:
				case FATAL:
					return false;
			}
		}
    	return true;
	}
    public MethodOutcome testResource(@ResourceParam IBaseResource resourceToValidate,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        log.info("Checking testresource" + validate_flag);
        MethodOutcome retVal = new MethodOutcome();
    	if(!validate_flag)
    	{
    	//	MethodOutcome retVal = new MethodOutcome();
    		retVal.setOperationOutcome(null);
    		return retVal;
    	}

		if (resourceToValidate == null) {
			Exception e = new InternalErrorException("Failed conversion to FHIR Resource. (Check resource type matches validation endpoint)");
			ProviderResponseLibrary.handleException(retVal,e);
			return retVal;
		}



		if (tkw_flag) {

			final HttpClient client1 = getHttpClient();
			final HttpPost request = new HttpPost(tkw_server);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
			request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
			try {

				request.setEntity(new StringEntity(ctx.newJsonParser().encodeResourceToString(resourceToValidate)));

				response = client1.execute(request);
				reader = new InputStreamReader(response.getEntity().getContent());

				IBaseResource resource = ctx.newJsonParser().parseResource(reader);
				if (resource instanceof OperationOutcome) {
					OperationOutcome operationOutcome = (OperationOutcome) resource;
					log.info("Issue Count = " + operationOutcome.getIssue().size());
					log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(operationOutcome));
					retVal.setOperationOutcome(operationOutcome);
				} else {
					throw new InternalErrorException("Server Error", (OperationOutcome) resource);
				}


			} catch (Exception e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
				// https://airelogic-apilabs.atlassian.net/browse/ALP4-922
				ProviderResponseLibrary.handleException(retVal, e);
			}
		} else {
			OperationOutcome outcome = validateResource(resourceToValidate);
			List<OperationOutcome.OperationOutcomeIssueComponent> issueRemove = new ArrayList<>();
			for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {
				Boolean remove = false;

				if (issue.getDiagnostics().contains("ValueSet http://snomed.info/sct not found")) {
					remove = true;
				}
				if (issue.getDiagnostics().contains("Could not verify slice for profile https://fhir.nhs.uk/STU3/StructureDefinition")) {
					remove = true;
				}
				if (issue.getDiagnostics().contains("http://snomed.info/sct")) {
					remove = true;
				}
				if (issue.getDiagnostics().contains("(fhirPath = true and (use memberOf 'https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NameUse-1'))")) {
					remove = true;
				}
				if (remove) {
					log.info("Stripped "+issue.getDiagnostics());
					issueRemove.add(issue);
				}
			}
			outcome.getIssue().removeAll(issueRemove);
			retVal.setOperationOutcome(outcome);
		}
        return retVal;

}


	public OperationOutcome validateResource(IBaseResource resource) {

		ValidationResult results = val.validateWithResult(resource);

		return (OperationOutcome) results.toOperationOutcome();

	}

}
