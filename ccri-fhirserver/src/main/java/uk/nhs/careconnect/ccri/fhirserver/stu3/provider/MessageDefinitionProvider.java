package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
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
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.MessageDefinitionRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class MessageDefinitionProvider implements ICCResourceProvider {


	@Autowired
	FhirContext ctx;

    @Autowired
    private MessageDefinitionRepository messageDefinitionDao;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
     
    @Override
    public Class<MessageDefinition> getResourceType() {
        return MessageDefinition.class;
    }


    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(MessageDefinitionProvider.class);

    @Override
    public Long count() {
        return messageDefinitionDao.count();
    }

    @Update()
    public MethodOutcome update(HttpServletRequest theRequest,@ResourceParam MessageDefinition messageDefinition) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            MessageDefinition newMessageDefinition = messageDefinitionDao.create(ctx, messageDefinition);
            method.setId(newMessageDefinition.getIdElement());
            method.setResource(newMessageDefinition);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<MessageDefinition> search(HttpServletRequest theRequest,
              @OptionalParam(name =MessageDefinition.SP_NAME) StringParam name,
               @OptionalParam(name =MessageDefinition.SP_PUBLISHER) StringParam publisher,
                                 @OptionalParam(name = MessageDefinition.SP_URL) UriParam url,
                                 @OptionalParam(name = MessageDefinition.SP_IDENTIFIER) TokenParam identifier
    ) {
        return messageDefinitionDao.search(ctx, name, publisher, url,identifier);
    }




    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam MessageDefinition messageDefinition) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            MessageDefinition newMessageDefinition = messageDefinitionDao.create(ctx, messageDefinition);
            method.setId(newMessageDefinition.getIdElement());
            method.setResource(newMessageDefinition);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Read
    public MessageDefinition get
            (@IdParam IdType internalId) {
    	resourcePermissionProvider.checkPermission("read");
        MessageDefinition messageDefinition = messageDefinitionDao.read(ctx, internalId);

        if ( messageDefinition == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No MessageDefinition/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return messageDefinition;
    }
    
    @Validate
    public MethodOutcome testResource(@ResourceParam MessageDefinition resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }


    @Operation(name = "$refresh", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getValueCodes(
            @OperationParam(name="id") TokenParam messageDefinitionId,
            @OperationParam(name="query") ReferenceParam messageDefinitionQuery

    ) throws Exception {

    	System.out.println("getting message definitions" + messageDefinitionQuery.getValue());
    	
    	HttpClient client1 = getHttpClient();
        HttpGet request = null;
    	if (messageDefinitionId != null) {
    	    request = new HttpGet(messageDefinitionQuery.getValue());
        }
        if (messageDefinitionQuery != null) {
            request = new HttpGet(messageDefinitionQuery.getValue());
        }

        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");

        Bundle bundle = getRequest(client1,request,messageDefinitionId);

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
                bundle = getRequest(client1,request,messageDefinitionId);
            }
            log.info("Iteration check = "+ next.toString());
        } while (next);

        log.info("Finished");

        MethodOutcome retVal = new MethodOutcome();
        return retVal;

    }

    private Bundle getRequest(HttpClient client1, HttpGet request, TokenParam messageDefinitionId) {
        Bundle bundle = null;
        HttpResponse response;
        Reader reader;
        try {
            //request.setEntity(new StringEntity(ctx.newJsonParser().encodeResourceToString(resourceToValidate)));
            response = client1.execute(request);
            reader = new InputStreamReader(response.getEntity().getContent());

            IBaseResource resource = ctx.newJsonParser().parseResource(reader);

            System.out.println("resource = " + resource);
            if(resource instanceof Bundle)
            {
                bundle = (Bundle) resource;

                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    //System.out.println(" messagedefinition id = " + entry.getResource().getIdElement().getIdPart() );
                    if (entry.hasResource() && entry.getResource() instanceof MessageDefinition
                            && (messageDefinitionId.getValue().contains("ALL")  || messageDefinitionId.getValue().contains(entry.getResource().getId())
                    ))
                    {
                        MessageDefinition vs = (MessageDefinition) entry.getResource();

                       // System.out.println("URL IS " + vs.getUrl());
                        HttpClient client2 = getHttpClient();


                        HttpGet request1 = new HttpGet(vs.getUrl());
                        request1.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
                        request1.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
                        try {

                            HttpResponse response1 = client2.execute(request1);


                            if(response1.getStatusLine().toString().contains("200")) {
                                //if (response.get .Content.Headers.ContentType.MediaType == "application/json")
                                reader = new InputStreamReader(response1.getEntity().getContent());


                                resource = ctx.newJsonParser().parseResource(reader);

                                if(resource instanceof MessageDefinition )
                                {
                                    MessageDefinition newVS = (MessageDefinition) resource;
                                    newVS.setName(newVS.getName()+ "..");
                                    List<MessageDefinition> results = messageDefinitionDao.search(ctx,null,null,new UriParam().setValue(newVS.getUrl()),null);
                                    if (results.size()>0) {
                                        System.out.println("Update existing resource with id = "+ results.get(0).getIdElement().getIdPart());
                                        newVS.setId(results.get(0).getIdElement().getIdPart());
                                    }
                                    MessageDefinition newMessageDefinition = messageDefinitionDao.create(ctx, newVS);
                                    System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(newMessageDefinition));
                                    System.out.println("newMessageDefinition.getIdElement()" + newMessageDefinition.getIdElement());
                                    // MessageDefinitionComposeComponent vscc = newVS.code .getCompose();
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
