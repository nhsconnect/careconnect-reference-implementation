package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.*;
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
import java.util.Set;

@Component
public class CarePlanResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(CarePlanResourceProvider.class);

    @Override
    public Class<CarePlan> getResourceType() {
        return CarePlan.class;
    }


    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle carePlanDocumentOperation(
            @IdParam IdType carePlanId

    ) throws Exception {
        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        // https://purple.testlab.nhs.uk/careconnect-ri/STU3/Encounter/804/$document?_count=50
        Exchange exchange = template.send("direct:FHIRCarePlanDocument",ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, "_count=50");
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, "CarePlan/"+carePlanId.getIdPart()+"/$document");
            }
        });
        inputStream = (InputStream) exchange.getIn().getBody();

        Bundle bundle = null;

        IBaseResource resource = null;
        try {
            String contents = org.apache.commons.io.IOUtils.toString(inputStream);
            resource = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);
        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                entry.getResource().setId(entry.getFullUrl().replace("urn:uuid:",""));
            }
           // log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource));
            return bundle;
            /*
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

                results.add(entry.getResource());
            }
            */
        } else  {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return null;

    }

    @Read
    public CarePlan getCarePlanById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();


        CarePlan carePlan = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream)  template.sendBody("direct:FHIRCarePlan",
                    ExchangePattern.InOut,httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRCarePlan",ExchangePattern.InOut, new Processor() {
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
        if (resource instanceof CarePlan) {
            carePlan = (CarePlan) resource;
        } else  {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return carePlan;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam CarePlan plan) throws Exception {



        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(plan);

            Exchange exchangeBundle = template.send("direct:FHIRCarePlan", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "CarePlan");
                }
            });

            resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof CarePlan) {
            plan = (CarePlan) resource;
        } else  {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);

        return method;
    }

    @Search
    public List<Resource> searchCarePlan(HttpServletRequest httpRequest,
                                         @OptionalParam(name = CarePlan.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CarePlan.SP_DATE) DateRangeParam date
            , @OptionalParam(name = CarePlan.SP_CATEGORY) TokenOrListParam categories
            , @OptionalParam(name = CarePlan.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CarePlan.SP_RES_ID) StringParam id
            , @IncludeParam(allow= {
            "CarePlan:subject"
            ,"CarePlan:supportingInplanation"
            , "*"}) Set<Include> includes
    ) throws Exception {

        List<Resource> results = new ArrayList<>();

        ProducerTemplate template = context.createProducerTemplate();



        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRCarePlan",
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
                Resource resource1 = entry.getResource();
                results.add(resource1);
            }
        } else  {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        return results;

    }


}
