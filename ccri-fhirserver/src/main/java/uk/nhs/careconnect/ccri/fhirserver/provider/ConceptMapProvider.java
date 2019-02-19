package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.nhs.careconnect.ri.dao.ConceptMapDao;
import uk.nhs.careconnect.ri.database.daointerface.ConceptMapRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class ConceptMapProvider implements IResourceProvider {
	
	@Autowired
	FhirContext ctx;
	
	@Autowired
    private ConceptMapRepository conceptMapDao;
	 
	@Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

	@Autowired
    private ResourceTestProvider resourceTestProvider;
	
	@Value("${ccri.conceptMapHost}")
	private String conceptMapHost;

    @Override
    public Class<ConceptMap> getResourceType() {
        return ConceptMap.class;
    }
    
    @Search
    public List<ConceptMap> search(HttpServletRequest theRequest,
                                                 @OptionalParam(name =ConceptMap.SP_NAME) StringParam name
    ) {
        return null;
    }
    
    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam ConceptMap conceptMap) {
    	ConceptMap newConceptMap = null;
    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        newConceptMap = conceptMapDao.create(conceptMap);
        method.setId(newConceptMap.getIdElement());
        method.setResource(newConceptMap);
        return method;
    }
    
    @Update()
    public MethodOutcome updateConceptMaps(HttpServletRequest theRequest,@ResourceParam  ConceptMap conceptMap) {
    		System.out.println("update method is called");
	    	resourcePermissionProvider.checkPermission("update");
	        MethodOutcome method = new MethodOutcome();
	        method.setCreated(true);
	        OperationOutcome opOutcome = new OperationOutcome();
	        method.setOperationOutcome(opOutcome);
	        ConceptMap newConceptMap = conceptMapDao.create(conceptMap);
	        method.setId(newConceptMap.getIdElement());
	        method.setResource(newConceptMap);
	        
        return method;
    }
    
    @Validate
    public MethodOutcome testResource(@ResourceParam ConceptMap resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    
    @Operation(name = "$refresh", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getConceptMaps(
            @OperationParam(name="conceptMapId") TokenParam
            			conceptMapId
    ) throws Exception {

    	System.out.println("getting value sets" + conceptMapId);
    	
    	final HttpClient client1 = getHttpClient();
        HttpGet request = new HttpGet(conceptMapHost);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
        HttpResponse response;
        Reader reader;
        
        try {
			response = client1.execute(request);
			reader = new InputStreamReader(response.getEntity().getContent());
			
			 IBaseResource resource = ctx.newJsonParser().parseResource(reader);
			 if(resource instanceof Bundle)
	         {
	            Bundle bundle = (Bundle) resource;	            
	            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
	            	System.out.println("  valueset id = " + conceptMapId.getValue().toString() );
	                if (entry.hasResource() && entry.getResource() instanceof ConceptMap
	                		&& (conceptMapId.getValue().toString().contains("ALL")  || conceptMapId.getValue().toString().contains(entry.getResource().getId().toString()) 
	                				))
	                		 {
	                	ConceptMap vs = (ConceptMap) entry.getResource();
	                	
	                	System.out.println("URL IS " + vs.getUrl());
	                	HttpClient client2 = getHttpClient();
	                	
	                	
	                	HttpGet request1 = new HttpGet(vs.getUrl());
	                	request1.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
	        	        request1.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
	        	        try 
	        	        {
	        	        	
	        	        	HttpResponse response1 = client2.execute(request1);
	        	       
		                	System.out.println(response1.getStatusLine());
		                	if(response1.getStatusLine().toString().contains("200")) 
		                	{
		                		reader = new InputStreamReader(response1.getEntity().getContent());
			        			resource = ctx.newJsonParser().parseResource(reader);
			        			if(resource instanceof ConceptMap )
		       			 			{
			        				ConceptMap newCM = (ConceptMap) resource;
		       			 			conceptMapDao.create(newCM);			       			 				
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
    	} catch (Exception e) 
        {
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

