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
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.GraphDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.GraphDefinitionRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class GraphDefinitionProvider implements ICCResourceProvider {


	@Autowired
	FhirContext ctx;

    @Autowired
    private GraphDefinitionRepository graphDao;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    @Override
    public Class<GraphDefinition> getResourceType() {
        return GraphDefinition.class;
    }

    @Override
    public Long count() {
        return graphDao.count();
    }

    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(GraphDefinitionProvider.class);


    @Update()
    public MethodOutcome update(HttpServletRequest theRequest,@ResourceParam GraphDefinition graph) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            GraphDefinition newGraphDefinition = graphDao.create(ctx, graph);
            method.setId(newGraphDefinition.getIdElement());
            method.setResource(newGraphDefinition);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<GraphDefinition> search(HttpServletRequest theRequest,
              @OptionalParam(name =GraphDefinition.SP_NAME) StringParam name,
               @OptionalParam(name =GraphDefinition.SP_PUBLISHER) StringParam publisher,
                                 @OptionalParam(name = GraphDefinition.SP_URL) UriParam url
    ) {
        return graphDao.search(ctx, name, publisher, url);
    }




    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam GraphDefinition graph) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            GraphDefinition newGraphDefinition = graphDao.create(ctx, graph);
            method.setId(newGraphDefinition.getIdElement());
            method.setResource(newGraphDefinition);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }
        return method;
    }

    @Read
    public GraphDefinition get
            (@IdParam IdType internalId) {
    	resourcePermissionProvider.checkPermission("read");
        GraphDefinition graph = graphDao.read(ctx, internalId);

        if ( graph == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No GraphDefinition/" + internalId.getIdPart()),
                    OperationOutcome.IssueType.NOTFOUND);
        }

        return graph;
    }
    
    @Validate
    public MethodOutcome testResource(@ResourceParam GraphDefinition resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }



    @Operation(name = "$refresh", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getValueCodes(
            @OperationParam(name="id") TokenParam graphId,
            @OperationParam(name="query") ReferenceParam graphQuery

    ) throws Exception {

    	System.out.println("getting value sets" + graphQuery.getValue());
    	
    	HttpClient client1 = getHttpClient();
        HttpGet request = null;
    	if (graphId != null) {
    	    request = new HttpGet(graphQuery.getValue());
        }
        if (graphQuery != null) {
            request = new HttpGet(graphQuery.getValue());
        }

        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");

        Bundle bundle = getRequest(client1,request,graphId);

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
                bundle = getRequest(client1,request,graphId);
            }
            log.info("Iteration check = "+ next.toString());
        } while (next);

        log.info("Finished");

        MethodOutcome retVal = new MethodOutcome();
        return retVal;

    }

    private Bundle getRequest(HttpClient client1, HttpGet request, TokenParam graphId) {
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
                    if (entry.hasResource() && entry.getResource() instanceof GraphDefinition
                            && (graphId.getValue().contains("ALL")  || graphId.getValue().contains(entry.getResource().getId())
                    ))
                    {
                        GraphDefinition vs = (GraphDefinition) entry.getResource();

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
                                if(resource instanceof GraphDefinition )
                                {
                                    GraphDefinition newVS = (GraphDefinition) resource;
                                    newVS.setName(newVS.getName()+ "..");
                                    List<GraphDefinition> results = graphDao.search(ctx,null,null,new UriParam().setValue(newVS.getUrl()));
                                    if (results.size()>0) {
                                        newVS.setId(results.get(0).getIdElement().getIdPart());
                                    }
                                    GraphDefinition newGraphDefinition = graphDao.create(ctx, newVS);
                                    System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(newGraphDefinition));
                                    System.out.println("newGraphDefinition.getIdElement()" + newGraphDefinition.getIdElement());
                                    // GraphDefinitionComposeComponent vscc = newVS.code .getCompose();
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
