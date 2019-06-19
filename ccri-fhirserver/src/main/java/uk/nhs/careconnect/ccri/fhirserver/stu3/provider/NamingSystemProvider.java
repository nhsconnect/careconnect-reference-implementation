package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
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
import uk.nhs.careconnect.ri.database.daointerface.NamingSystemRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.List;

@Component
public class NamingSystemProvider implements ICCResourceProvider {


	@Autowired
	FhirContext ctx;

    @Autowired
    private NamingSystemRepository namingSystemDao;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    
    @Override
    public Class<NamingSystem> getResourceType() {
        return NamingSystem.class;
    }

    @Override
    public Long count() {
        return namingSystemDao.count();
    }

    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(NamingSystemProvider.class);


    @Update()
    public MethodOutcome update(HttpServletRequest theRequest,@ResourceParam NamingSystem namingSystem) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            NamingSystem newNamingSystem = namingSystemDao.create(ctx, namingSystem);
            method.setId(newNamingSystem.getIdElement());
            method.setResource(newNamingSystem);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<NamingSystem> search(HttpServletRequest theRequest,
              @OptionalParam(name =NamingSystem.SP_NAME) StringParam name,
               @OptionalParam(name =NamingSystem.SP_PUBLISHER) StringParam publisher,
             @OptionalParam(name = NamingSystem.SP_VALUE) TokenParam unique
    ) {
        return namingSystemDao.search(ctx, name, publisher, unique);
    }




    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam NamingSystem namingSystem) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        try {
            NamingSystem newNamingSystem = namingSystemDao.create(ctx, namingSystem);
            method.setId(newNamingSystem.getIdElement());
            method.setResource(newNamingSystem);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Read
    public NamingSystem get
            (@IdParam IdType internalId) {
    	resourcePermissionProvider.checkPermission("read");
        NamingSystem namingSystem = namingSystemDao.read(ctx, internalId);

        if ( namingSystem == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No NamingSystem/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return namingSystem;
    }
    
    @Validate
    public MethodOutcome testResource(@ResourceParam NamingSystem resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }



    @Operation(name = "$refresh", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getValueCodes(
            @OperationParam(name="id") TokenParam namingSystemId,
            @OperationParam(name="query") ReferenceParam namingSystemQuery

    ) throws Exception {

    	System.out.println("getting value sets" + namingSystemQuery.getValue());
    	
    	HttpClient client1 = getHttpClient();
        HttpGet request = null;
    	if (namingSystemId != null) {
    	    request = new HttpGet(namingSystemQuery.getValue());
        }
        if (namingSystemQuery != null) {
            request = new HttpGet(namingSystemQuery.getValue());
        }

        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");

        Bundle bundle = getRequest(client1,request,namingSystemId);

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
                bundle = getRequest(client1,request,namingSystemId);
            }
            log.info("Iteration check = "+ next.toString());
        } while (next);

        log.info("Finished");

        MethodOutcome retVal = new MethodOutcome();
        return retVal;
        
    }

    private Bundle getRequest(HttpClient client1, HttpGet request, TokenParam namingSystemId) {
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
                    System.out.println("  namingSystem id = " + entry.getResource().getId() );
                    if (entry.hasResource() && entry.getResource() instanceof NamingSystem
                            && (namingSystemId.getValue().contains("ALL")  || namingSystemId.getValue().contains(entry.getResource().getId())
                    ))
                    {
                        NamingSystem vs = (NamingSystem) entry.getResource();
                        try {
                            List<NamingSystem> results = namingSystemDao.search(ctx,null,null,new TokenParam().setValue(vs.getUrl()));
                            if (results.size()>0) {
                                vs.setId(results.get(0).getIdElement().getIdPart());
                            }
                            NamingSystem newNamingSystem = namingSystemDao.create(ctx, vs);
                            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(newNamingSystem));
                            System.out.println("newNamingSystem.getIdElement()" + newNamingSystem.getIdElement());
                            // NamingSystemComposeComponent vscc = newVS.code .getCompose();
                            System.out.println("code concept" + vs.getId());

                        }
                        catch(Exception e) {System.out.println(e.getMessage());}

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
