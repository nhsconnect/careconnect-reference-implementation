package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
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
public class LocationResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(LocationResourceProvider.class);

    @Override
    public Class<Location> getResourceType() {
        return Location.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam Location resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    */

    @Read
    public Location getLocationById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();


        Location location = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream)  template.sendBody("direct:FHIRLocation",
                    ExchangePattern.InOut,httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRLocation",ExchangePattern.InOut, new Processor() {
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
        if (resource instanceof Location) {
            location = (Location) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return location;
    }

    @Search
    public List<Location> searchLocation(HttpServletRequest httpRequest,
                                         @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifierCode,
                                         @OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) StringParam postCode
            , @OptionalParam(name = Location.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<Location> results = new ArrayList<Location>();

        ProducerTemplate template = context.createProducerTemplate();



        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRLocation",
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
                Location patient = (Location) entry.getResource();
                results.add(patient);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Location location) throws Exception {


        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(location);

            Exchange exchangeBundle = template.send("direct:FHIRLocation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Location");
                }
            });

            // This response is coming from an external FHIR Server, so uses inputstream
            resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof Location) {
            location = (Location) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);

        return method;
    }


    @Update
    public MethodOutcome updateLocation(HttpServletRequest theRequest, @ResourceParam Location location, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(location);


            Exchange exchangeBundle = template.send("direct:FHIRLocation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "PUT");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Location/"+theId.getIdPart());
                }
            });

            resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof Location) {
            location = (Location) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);

        return method;
    }




}
