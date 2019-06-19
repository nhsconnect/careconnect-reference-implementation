package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


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
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.OperationOutcome;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class ResourceTestProvider {


	@Value("${ccri.validate_use_tkw}")
	private Boolean tkw_flag;

	@Qualifier("stu3ctx")
    @Autowired()
    FhirContext ctx;

	@Qualifier("r4ctx")
	@Autowired()
	FhirContext r4ctx;

 //   @Autowired
//	FhirInstanceValidator instanceValidator;

	@Qualifier("fhirValidatorSTU3")
    @Autowired
    FhirValidator val;
    
	HttpResponse response;
	Reader reader;
    private static final Logger log = LoggerFactory.getLogger(ResourceTestProvider.class);

    
    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }


    public MethodOutcome testResource(@ResourceParam IBaseResource resourceToValidate,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {

        MethodOutcome retVal = new MethodOutcome();
    	if(!HapiProperties.getValidationFlag())
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
			final HttpPost request = new HttpPost(HapiProperties.getValidationServer());
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
			outcome = OperationOutcomeFactory.removeUnsupportedIssues(outcome);


			retVal.setOperationOutcome(outcome);
		}
        return retVal;

}


	public OperationOutcome validateResource(IBaseResource resource) {


		if (true) {
			// stu3
			log.trace(this.ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource));
			ValidationResult results = val.validateWithResult(resource);
			return OperationOutcomeFactory.removeUnsupportedIssues((OperationOutcome) results.toOperationOutcome());
		} else {
			// r4
			log.info(resource.getClass().getCanonicalName());
			org.hl7.fhir.r4.model.Resource convertedResource = VersionConvertor_30_40.convertResource((org.hl7.fhir.dstu3.model.Resource) resource, true);
			log.info(convertedResource.getClass().getCanonicalName());
			log.info(this.r4ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(convertedResource));

			ValidationResult results = val.validateWithResult(convertedResource);

			return OperationOutcomeFactory.removeUnsupportedIssues((org.hl7.fhir.r4.model.OperationOutcome) results.toOperationOutcome(), this.r4ctx);
		}

	}

}
