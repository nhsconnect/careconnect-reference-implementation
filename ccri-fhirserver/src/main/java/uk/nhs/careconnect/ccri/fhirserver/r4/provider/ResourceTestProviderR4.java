package uk.nhs.careconnect.ccri.fhirserver.r4.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.nhs.careconnect.ccri.fhirserver.support.MessageInstanceValidator;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.MessageDefinitionRepository;

import java.io.Reader;

@Component
public class ResourceTestProviderR4 {


	@Value("${ccri.validate_use_tkw}")
	private Boolean tkw_flag;

	@Qualifier("r4ctx")
    @Autowired()
    FhirContext ctx;

	@Qualifier("fhirValidatorR4")
    @Autowired
    FhirValidator val;

	@Autowired
	private MessageDefinitionRepository messageDefinitionDao;

	@Autowired
	private MessageInstanceValidator messageInstanceValidator;
    
	HttpResponse response;
	Reader reader;
    private static final Logger log = LoggerFactory.getLogger(ResourceTestProviderR4.class);

    
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
		OperationOutcome outcome = validateResource(resourceToValidate, theMode, theProfile);
		// TODO outcome = OperationOutcomeFactory.removeUnsupportedIssues(outcome, ctx);


		retVal.setOperationOutcome(outcome);
		return retVal;

	}



	public OperationOutcome validateResource(IBaseResource resource,ValidationModeEnum theMode,
                                              String theProfile) {

		log.trace(this.ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource));

		ValidationOptions options = new ValidationOptions();
        if (theProfile != null) options.addProfile(theProfile);

		ValidationResult results = val.validateWithResult(resource,options);

		if (resource instanceof Bundle) {


			//OperationOutcome message = messageInstanceValidator.validateMessageBundle((Bundle) resource);
		}


		// OperationOutcome result = (OperationOutcome) results.toOperationOutcome();

		return (OperationOutcome) results.toOperationOutcome();

	}

}
