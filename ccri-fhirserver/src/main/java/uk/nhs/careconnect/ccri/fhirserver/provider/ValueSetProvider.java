package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;


import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.HttpClientBuilder;

import org.hl7.fhir.dstu3.model.*;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;

import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class ValueSetProvider implements IResourceProvider {


	@Autowired
	FhirContext ctx;

    @Autowired
    private ValueSetRepository valueSetDao;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    @Value("${ccri.valueSetHost}")
	private String valueSetHost;
    
    @Override
    public Class<ValueSet> getResourceType() {
        return ValueSet.class;
    }


    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(ValueSetProvider.class);


    @Update()
    public MethodOutcome update(HttpServletRequest theRequest,@ResourceParam ValueSet valueSet) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            ValueSet newValueSet = valueSetDao.create(ctx, valueSet);
            method.setId(newValueSet.getIdElement());
            method.setResource(newValueSet);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<ValueSet> search(HttpServletRequest theRequest,
              @OptionalParam(name =ValueSet.SP_NAME) StringParam name,
               @OptionalParam(name =ValueSet.SP_PUBLISHER) StringParam publisher
    ) {
        return valueSetDao.searchValueset(ctx, name, publisher);
    }




    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam ValueSet valueSet) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            ValueSet newValueSet = valueSetDao.create(ctx, valueSet);
            method.setId(newValueSet.getIdElement());
            method.setResource(newValueSet);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Read
    public ValueSet get
            (@IdParam IdType internalId) {
    	resourcePermissionProvider.checkPermission("read");
        ValueSet valueSet = valueSetDao.read(ctx, internalId);

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

    @Operation(name = "$expand", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public ValueSet expand(
            @IdParam IdType internalId

    ) throws Exception {
        resourcePermissionProvider.checkPermission("read");
        ValueSet valueSet = valueSetDao.readAndExpand(ctx, internalId);

        if ( valueSet == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No ValueSet/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return valueSet;

    }

    @Operation(name = "$getvaluecodes", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getValueCodes(
            @OperationParam(name="valueSetId") TokenParam valueSetId,
            @OperationParam(name="valueSetQuery") ReferenceParam valueSetQuery

    ) throws Exception {

    	System.out.println("getting value sets" + valueSetHost);
    	
    	HttpClient client1 = getHttpClient();
        HttpGet request = null;
    	if (valueSetId != null) {
    	    request = new HttpGet(valueSetHost);
        }
        if (valueSetQuery != null) {
            request = new HttpGet(valueSetQuery.getValue());
        }

        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");

        Bundle bundle = getRequest(client1,request,valueSetId);

        Boolean next = false;
        do {
            next = false;
            for (Bundle.BundleLinkComponent link : bundle.getLink()) {
                if (link.getRelation().equals("next")) {
                    next = true;
                    client1 = getHttpClient();
                    request = new HttpGet(link.getUrl());
                    request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
                    request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
                }
            }
            if (next) {
                log.info("Get next bundle "+request.getURI());
                bundle = getRequest(client1,request,valueSetId);
            }
            log.info("Iteration check = "+ next.toString());
        } while (next);

        log.info("Finished");

        MethodOutcome retVal = new MethodOutcome();
        return retVal;
        
    }

    private Bundle getRequest(HttpClient client1, HttpGet request, TokenParam valueSetId) {
        Bundle bundle = null;
        HttpResponse response;
        Reader reader;
        try {
            //request.setEntity(new StringEntity(ctx.newJsonParser().encodeResourceToString(resourceToValidate)));
            response = client1.execute(request);
            reader = new InputStreamReader(response.getEntity().getContent());

            IBaseResource resource = ctx.newJsonParser().parseResource(reader);
            //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource));

            System.out.println("resource = " + resource);
            if(resource instanceof Bundle)
            {
                bundle = (Bundle) resource;
                System.out.println("Entry Count = " + bundle.getEntry().size());
                System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));
                //retVal.setOperationOutcome(operationOutcome);

                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    System.out.println("  valueset id = " + entry.getResource().getId() );
                    if (entry.hasResource() && entry.getResource() instanceof ValueSet
                            && (valueSetId.getValue().contains("ALL")  || valueSetId.getValue().contains(entry.getResource().getId())
                    ))
                    {
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
                                    ValueSet newValueSet = valueSetDao.create(ctx, newVS);
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
        return bundle;
    }
    
    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }
    
}
