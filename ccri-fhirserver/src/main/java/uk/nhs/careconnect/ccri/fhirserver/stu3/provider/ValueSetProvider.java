package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.*;
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
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;

import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class ValueSetProvider implements ICCResourceProvider {


	@Autowired
	FhirContext ctx;

	IGenericClient client;

    @Autowired
    private ValueSetRepository valueSetDao;

    @Autowired
    private ValidationSupportProvider validationSupportProvider;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    @Override
    public Class<ValueSet> getResourceType() {
        return ValueSet.class;
    }

    @Override
    public Long count() {
        return valueSetDao.count();
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
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<ValueSet> search(HttpServletRequest theRequest,
              @OptionalParam(name =ValueSet.SP_NAME) StringParam name,
             //  @OptionalParam(name =ValueSet.SP_PUBLISHER) StringParam publisher,
                                 @OptionalParam(name = ValueSet.SP_URL) UriParam url,
                                @OptionalParam(name = ValueSet.SP_IDENTIFIER) TokenParam identifier
    ) {
        return valueSetDao.search(ctx, name, null, url,identifier);
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
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
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
                   OperationOutcome.IssueType.NOTFOUND);
        }

        return valueSet;
    }
    
    @Validate
    public MethodOutcome testResource(@ResourceParam ValueSet resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @OptionalParam(name = "profile") @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

    @Operation(name = "$expand", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public ValueSet expand(
            @IdParam IdType internalId

    ) throws Exception {
        resourcePermissionProvider.checkPermission("read");
        ValueSet valueSet = valueSetDao.readAndExpand(ctx, internalId);

        if (!valueSet.hasExpansion()&&valueSet.hasCompose()) {
            if (valueSet.getCompose().hasInclude()) {
               for (ValueSet.ConceptSetComponent include : valueSet.getCompose().getInclude()) {
                   if (include.hasSystem() && include.getSystem().equals("http://snomed.info/sct")) {
                       log.info("SNOMED");

                       // http://hl7.org/fhir/snomedct.html

                       if (client == null) {
                           client = ctx.newRestfulGenericClient(HapiProperties.getTerminologyServer());
                       }
                       for (ValueSet.ConceptSetFilterComponent filter : include.getFilter()) {
                           if (filter.hasOp()) {
                               log.info("has Filter");
                               ValueSet vsExpansion = null;
                               switch (filter.getOp()) {
                                   case IN:
                                       log.info("IN Filter detected");

                                       vsExpansion = (ValueSet) client
                                               .operation()
                                               .onType(ValueSet.class)
                                               .named("expand")
                                               .withSearchParameter(Parameters.class,"identifier", new UriParam(HapiProperties.getSnomedVersionUrl()+"?fhir_vs=refset/"+filter.getValue()))
                                               .returnResourceType(ValueSet.class)
                                               .useHttpGet()
                                               .execute();

                                       break;
                                   case EQUAL:
                                       log.info("EQUAL Filter detected - {}", filter.getValue());
                                      String url = HapiProperties.getSnomedVersionUrl()+"?fhir_vs=ecl/"+filter.getValue();
                                       //url = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
                                       log.info(url);
                                       url = url.replace("^","%5E");
                                       url = url.replace("|","%7C");
                                       url = url.replace("<","%3C");
                                       log.info(url);
                                       vsExpansion = (ValueSet) client
                                               .operation()
                                               .onType(ValueSet.class)
                                               .named("expand")
                                               .withSearchParameter(Parameters.class,"identifier", new UriParam(url))
                                               .returnResourceType(ValueSet.class)
                                               .useHttpGet()
                                               .execute();
                               }
                               if (vsExpansion != null) {
                                   log.info("EXPANSION RETURNED");
                                   log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(vsExpansion));
                                   if (vsExpansion.hasExpansion()) {
                                       for (ValueSet.ValueSetExpansionContainsComponent contains : vsExpansion.getExpansion().getContains()) {
                                           try {
                                               valueSet.getExpansion().addContains(contains);
                                           } catch (Exception ex) {
                                                System.out.println(ex.getMessage());
                                           }
                                       }
                                   }

                               }
                           }
                       }
                   }
                }
            }
        }
        if ( valueSet == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No ValueSet/" + internalId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return valueSet;

    }



    @Operation(name = "$refresh", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public MethodOutcome getValueCodes(
            @OperationParam(name="id") TokenParam valueSetId,
            @OperationParam(name="query") ReferenceParam valueSetQuery

    ) throws Exception {

    	System.out.println("getting value sets" + HapiProperties.getTerminologyServerSecondary());
    	
    	HttpClient client1 = getHttpClient();
        HttpGet request = null;
    	if (valueSetId != null) {
    	    request = new HttpGet(HapiProperties.getTerminologyServerSecondary()+"ValueSet/");
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
                                    List<ValueSet> results = valueSetDao.search(ctx,null,null,new UriParam().setValue(newVS.getUrl()),null);
                                    if (results.size()>0) {
                                        newVS.setId(results.get(0).getIdElement().getIdPart());
                                    }
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
