package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
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
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.StructureDefinitionRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class StructureDefinitionProvider implements IResourceProvider {

	@Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
   
	@Autowired
    private ResourceTestProvider resourceTestProvider;

    @Override
    public Class<StructureDefinition> getResourceType() {
        return StructureDefinition.class;
    }

    @Autowired
    private StructureDefinitionRepository structureDefinitionDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(StructureDefinitionProvider.class);

    /*
    @Override
    public Long count() {
        return structureDefinitionDao.count();
    }
*/
    @Search
    public List<StructureDefinition> search(HttpServletRequest theRequest,
                                            @OptionalParam(name = StructureDefinition.SP_NAME) StringParam name,
                                            @OptionalParam(name = StructureDefinition.SP_PUBLISHER) StringParam publisher,
                                            @OptionalParam(name = StructureDefinition.SP_URL) UriParam url
    ) {
        return structureDefinitionDao.search(ctx, name, publisher, url);
    }

    @Read
    public StructureDefinition get
            (@IdParam IdType internalId) {
        resourcePermissionProvider.checkPermission("read");
        StructureDefinition structureDefinition = structureDefinitionDao.read( ctx, internalId);

        if ( structureDefinition == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No StructureDefinition/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return structureDefinition;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam StructureDefinition structureDefinition)

    {
        log.info("create method is called");
        resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();

        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            StructureDefinition newStructureDefinition = structureDefinitionDao.create(ctx,structureDefinition);
            method.setCreated(true);
            method.setId(newStructureDefinition.getIdElement());
            method.setResource(newStructureDefinition);
        } catch (Exception ex) {
            log.info(ex.getMessage());
            ProviderResponseLibrary.handleException(method,ex);
        }
        return method;
    }

    @Update()
    public MethodOutcome update(HttpServletRequest theRequest,@ResourceParam  StructureDefinition structureDefinition) throws OperationOutcomeException {
        log.info("update method is called");
        resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();

        try {
            StructureDefinition newStructureDefinition = structureDefinitionDao.create(ctx,structureDefinition);
            method.setCreated(false);
            method.setId(newStructureDefinition.getIdElement());
            method.setResource(newStructureDefinition);
        } catch (Exception ex) {
            log.info(ex.getMessage());
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam StructureDefinition resource,
                                      @Validate.Mode ValidationModeEnum theMode,
                                      @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

    @Operation(name = "$refresh", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome refresh(
            @OperationParam(name="id") TokenParam structureDefinitionId,
            @OperationParam(name="query") ReferenceParam structureDefinitionQuery

    ) throws Exception {


        HttpClient client1 = getHttpClient();
        HttpGet request = null;
        if (structureDefinitionId != null) {
            request = new HttpGet(structureDefinitionQuery.getValue());
        }
        if (structureDefinitionQuery != null) {
            request = new HttpGet(structureDefinitionQuery.getValue());
        }

        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");

        Bundle bundle = getRequest(client1,request,structureDefinitionId);

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
                bundle = getRequest(client1,request,structureDefinitionId);
            }
            log.info("Iteration check = "+ next.toString());
        } while (next);

        log.info("Finished");

        MethodOutcome retVal = new MethodOutcome();
        return retVal;

    }

    private Bundle getRequest(HttpClient client1, HttpGet request, TokenParam structureDefinitionId) {
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
                    if (entry.hasResource() && entry.getResource() instanceof StructureDefinition
                            && (structureDefinitionId.getValue().contains("ALL")  || structureDefinitionId.getValue().contains(entry.getResource().getId())
                    ))
                    {
                        StructureDefinition vs = (StructureDefinition) entry.getResource();

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
                                if(resource instanceof StructureDefinition )
                                {
                                    StructureDefinition newVS = (StructureDefinition) resource;
                                    newVS.setName(newVS.getName()+ "..");
                                    List<StructureDefinition> results = structureDefinitionDao.search(ctx,null,null,new UriParam().setValue(newVS.getUrl()));
                                    if (results.size()>0) {
                                        newVS.setId(results.get(0).getIdElement().getIdPart());
                                    }
                                    StructureDefinition newStructureDefinition = structureDefinitionDao.create(ctx, newVS);
                                    System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(newStructureDefinition));
                                    System.out.println("newStructureDefinition.getIdElement()" + newStructureDefinition.getIdElement());
                                    // StructureDefinitionComposeComponent vscc = newVS.code .getCompose();
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
