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
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class CodeSystemProvider implements ICCResourceProvider {

	@Autowired
    private CodeSystemRepository codeSystemDao;
	
	@Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

	@Autowired
    private ResourceTestProvider resourceTestProvider;

    @Override
    public Class<CodeSystem> getResourceType() {
        return CodeSystem.class;
    }

    @Autowired
    FhirContext ctx;

    @Override
    public Long count() {
        return codeSystemDao.count();
    }

    private static final Logger log = LoggerFactory.getLogger(CodeSystemProvider.class);

    @Search
    public List<CodeSystem> search(HttpServletRequest theRequest,
                 @OptionalParam(name =CodeSystem.SP_NAME) StringParam name,
               //  @OptionalParam(name = CodeSystem.SP_PUBLISHER) StringParam publisher,
                 @OptionalParam(name = CodeSystem.SP_URL) UriParam url)

    {
        return codeSystemDao.search(ctx, name, null,url);
    }

    @Read
    public CodeSystem get
            (@IdParam IdType internalId) {
        resourcePermissionProvider.checkPermission("read");
        CodeSystem codeSystem = codeSystemDao.read(ctx, internalId);

        if ( codeSystem == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No CodeSystem/" + internalId.getIdPart()),
                    OperationOutcome.IssueType.NOTFOUND);
        }

        return codeSystem;
    }

    @Update()
    public MethodOutcome update(HttpServletRequest theRequest,@ResourceParam CodeSystem codeSystem) {

        resourcePermissionProvider.checkPermission("update");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            CodeSystem newCodeSystem = codeSystemDao.create(ctx, codeSystem);
            method.setId(newCodeSystem.getIdElement());
            method.setResource(newCodeSystem);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }


    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam CodeSystem codeSystem) {

    	resourcePermissionProvider.checkPermission("create");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            CodeSystem newCodeSystem = codeSystemDao.create(ctx, codeSystem);
            method.setId(newCodeSystem.getIdElement());
            method.setResource(newCodeSystem);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }


    @Validate
    public MethodOutcome testResource(@ResourceParam CodeSystem resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                   @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }


    @Operation(name = "$refresh", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getCodeSystems(
            @OperationParam(name="id") TokenParam codeSystemId,
            @OperationParam(name="query") ReferenceParam codeSystemQuery

    ) throws Exception {

        System.out.println("getting codeSystem" + codeSystemQuery.getValue());

        HttpClient client1 = getHttpClient();
        HttpGet request = null;
        if (codeSystemId != null) {
            request = new HttpGet(codeSystemQuery.getValue());
        }
        if (codeSystemQuery != null) {
            request = new HttpGet(codeSystemQuery.getValue());
        }

        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");

        Bundle bundle = getRequest(client1,request,codeSystemId);

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
                bundle = getRequest(client1,request,codeSystemId);
            }
            log.info("Iteration check = "+ next.toString());
        } while (next);

        log.info("Finished");

        MethodOutcome retVal = new MethodOutcome();
        return retVal;

    }

    private Bundle getRequest(HttpClient client1, HttpGet request, TokenParam codeSystemId) {
        Bundle bundle = null;
        HttpResponse response;
        Reader reader;
        try {
            //request.setEntity(new StringEntity(ctx.newJsonParser().encodeResourceToString(resourceToValidate)));
            response = client1.execute(request);
            reader = new InputStreamReader(response.getEntity().getContent());

            IBaseResource resource = ctx.newJsonParser().parseResource(reader);

          //  System.out.println("resource = " + resource);
            if(resource instanceof Bundle)
            {
                bundle = (Bundle) resource;
                System.out.println("Entry Count = " + bundle.getEntry().size());
            //    System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));
                //retVal.setOperationOutcome(operationOutcome);

                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                 //   System.out.println("  codesystem id = " + entry.getResource().getId() );
                    if (entry.hasResource() && entry.getResource() instanceof CodeSystem
                            && (codeSystemId.getValue().contains("ALL")  || codeSystemId.getValue().contains(entry.getResource().getId())
                    ))
                    {
                        CodeSystem vs = (CodeSystem) entry.getResource();

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
                                if(resource instanceof CodeSystem )
                                {
                                    CodeSystem newVS = (CodeSystem) resource;
                                    newVS.setName(newVS.getName()+ "..");
                                    System.out.println("Search for existing CodeSystem = "+newVS.getUrl());
                                    List<CodeSystem> results = codeSystemDao.search(ctx,null,null,new UriParam().setValue(newVS.getUrl()));
                                    if (results.size()>0) {
                                        System.out.println("Found "+results.size()+" entries");
                                        newVS.setId(results.get(0).getIdElement().getIdPart());
                                    } else {
                                        System.out.println("Not found CS for "+newVS.getUrl());
                                    }
                                    CodeSystem newCodeSystem = codeSystemDao.create(ctx, newVS);

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
