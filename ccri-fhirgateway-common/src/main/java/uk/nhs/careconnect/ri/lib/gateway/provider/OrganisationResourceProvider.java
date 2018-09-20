package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrganisationResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(OrganisationResourceProvider.class);

    @Override
    public Class<Organization> getResourceType() {
        return Organization.class;
    }

/*
    @Validate
    public MethodOutcome testResource(@ResourceParam Organization resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
*/

    @Read
    public Organization getOrganizationById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();


        Organization organization = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream) template.sendBody("direct:FHIROrganisation",
                        ExchangePattern.InOut, httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIROrganisation",ExchangePattern.InOut, new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "/"+internalId.getValue());
                    }
                });
                inputStream = (InputStream) exchange.getIn().getBody();
            }
            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Organization) {
            organization = (Organization) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        return organization;
    }

    @Search
    public List<Organization> searchOrganization(HttpServletRequest httpRequest,
                                                 @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier,
                                                 @OptionalParam(name = Organization.SP_NAME) StringParam name
            , @OptionalParam(name = Organization.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<Organization> results = new ArrayList<Organization>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = (InputStream) template.sendBody("direct:FHIROrganisation",
                ExchangePattern.InOut,httpRequest);

        Bundle bundle = null;
        Reader reader = new InputStreamReader(inputStream);
        IBaseResource resource = null;
        try {
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Organization patient = (Organization) entry.getResource();
                results.add(patient);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
