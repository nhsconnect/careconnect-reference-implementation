package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
/*import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;*/
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.dstu3.model.ValueSet.ValueSetComposeComponent;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;
//import uk.nhs.careconnect.ri.messaging.support.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class ValueSetProvider implements IResourceProvider {

	@Autowired
	CamelContext context;

	@Autowired
	FhirContext ctx;

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

    @Operation(name = "$getvaluecodes", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getValueCodes(
         /*   @OperationParam(name="patientNHSnumber") TokenParam
                    nhsNumber,
            @OperationParam(name="recordType") TokenParam
                    recordType,
            @OperationParam(name="recordSection") TokenOrListParam
                    recordSection*/
    ) throws Exception {

    	System.out.println("getting value sets");
    	
    	final HttpClient client1 = getHttpClient();
         HttpGet request = new HttpGet("http://localhost:8186/ccri-fhir/STU3/ValueSet/");
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
        HttpResponse response;
        Reader reader;
        
        try {
			//request.setEntity(new StringEntity(ctx.newJsonParser().encodeResourceToString(resourceToValidate)));
			response = client1.execute(request);
			reader = new InputStreamReader(response.getEntity().getContent());
			
			 IBaseResource resource = ctx.newJsonParser().parseResource(reader);
			 System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource));
			 
			   System.out.println("resource = " + resource);
			 if(resource instanceof Bundle)
	         {
	            Bundle bundle = (Bundle) resource;
	            System.out.println("Entry Count = " + bundle.getEntry().size());
	            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));
	            //retVal.setOperationOutcome(operationOutcome);
	            
	            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
	                if (entry.hasResource() && entry.getResource() instanceof ValueSet) {
	                	ValueSet vs = (ValueSet) entry.getResource();
	                	
	                	System.out.println("URL IS " + vs.getUrl());
	                	HttpClient client2 = getHttpClient();
	                	
	                	
	                	HttpGet request1 = new HttpGet(vs.getUrl());
	                	request1.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
	        	        request1.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
	        	        try {
	        	        	
	        	        	HttpResponse response1 = client2.execute(request1);
	        	       
	                	System.out.println(response1.getStatusLine());
	                	if(response1.getStatusLine().toString().contains("200")) {
	                	//if (response.get .Content.Headers.ContentType.MediaType == "application/json")
	        			reader = new InputStreamReader(response1.getEntity().getContent());
	        			
	        	        
	        			resource = ctx.newJsonParser().parseResource(reader);
	       			 	System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource));
	       			 	if(resource instanceof ValueSet )
	       			 	{
	    	            ValueSet newVS = (ValueSet) resource;
	    	            newVS.setName(newVS.getName()+ "..");
	    	            ValueSet newValueSet = valueSetDao.create(newVS);
	    	            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(newValueSet));
	    	            System.out.println("newValueSet.getIdElement()" + newValueSet.getIdElement());
	    	           // ValueSetComposeComponent vscc = newVS.code .getCompose();
	    	           System.out.println("code concept" + newVS.getId());
	    	            
	       			 	}
	                	}
	        	        } 
	        	        catch(UnknownHostException e) {System.out.println("Host not known");}
	       			 	
	                }
	            }
	            
	            
	         }
	         else
	         {
	            throw new InternalErrorException("Server Error", (OperationOutcome) resource);
	         }

		
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        
        MethodOutcome retVal = new MethodOutcome();
        return retVal;
        
    }
    
    
    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }
    
}
