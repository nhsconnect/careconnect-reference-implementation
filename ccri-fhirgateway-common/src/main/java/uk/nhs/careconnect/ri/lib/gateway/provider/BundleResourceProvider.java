package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
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
import java.io.*;

@Component
public class BundleResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(BundleResourceProvider.class);

    @Override
    public Class<Bundle> getResourceType() {
        return Bundle.class;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Bundle bundle,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(bundle,theMode,theProfile);
    }

    private Exchange buildBundlePost(Exchange exchange, String newXmlResource, String query, String method) {
        exchange.getIn().setBody(newXmlResource);
        exchange.getIn().setHeader(Exchange.HTTP_QUERY, query);
        exchange.getIn().setHeader(Exchange.HTTP_METHOD, method);
        exchange.getIn().setHeader(Exchange.HTTP_PATH, "Bundle");
        // exchange.getIn().setHeader("Prefer", "return=representation");
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
        return exchange;
    }



    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Bundle bundle) throws Exception {


        // Example message https://gist.github.com/IOPS-DEV/1a532eb43b226dcd6ce26a6b698019f4#file-ec_edischarge_full_payload_example-01

        /*

        Task to do here.



        Ensure the message can be accepted. Validate the message.
        (may need to convert from json to xml - currently throws an error if not xml)

        if ok send onto camel for processing (Async)

        build reply.

        P.S. We are using XML here, normally we use JSON but bundles in the NHS are primarily XML at present.

         */

        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(bundle);

            switch (bundle.getType()) {
                case COLLECTION:

                    Exchange exchangeBundle = template.send("direct:FHIRBundleCollection", ExchangePattern.InOut, new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            exchange = buildBundlePost(exchange,newXmlResource,null,"POST");

                        }
                    });
                    log.trace("IN MESSAGE POST HANDLING");
                    // This response is coming from an external FHIR Server, so uses inputstream
                    resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());
                    break;

                case MESSAGE:
                    // Sync to get response

                    // ASync This uses a queue direct:FHIRBundleCollection
                    // Sync Direct flow direct:FHIRBundleMessage
                    Exchange exchangeMessage = template.send("direct:FHIRBundleMessage", ExchangePattern.InOut, new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            exchange = buildBundlePost(exchange,newXmlResource,null,"POST");

                        }
                    });
                    resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeMessage.getIn().getBody());
                    break;

                case DOCUMENT:
                    // Send a copy for EPR processing - Consider moving to camel route

                    // Main Message send to EDMS
                    Exchange exchangeDocument = template.send("direct:FHIRBundleDocumentCreate", ExchangePattern.InOut, new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            exchange = buildBundlePost(exchange,newXmlResource,null,"POST");

                        }
                    });
                    resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeDocument.getIn().getBody());

                    default:
                        // TODO
            }
        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);

        return method;
    }


    @Update
    public MethodOutcome updateBundle(HttpServletRequest theRequest, @ResourceParam Bundle bundle, @IdParam IdType bundleId, @ConditionalUrlParam String conditional, RequestDetails theRequestDetails) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(bundle);

            switch (bundle.getType()) {
                case COLLECTION:
                case MESSAGE:

                    Exchange exchangeBundle = template.send("direct:FHIRBundleCollection", ExchangePattern.InOut, new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            exchange = buildBundlePost(exchange,newXmlResource,conditional,"PUT");

                        }
                    });
                    // TODO need proper responses from the camel processor. KGM 18/Apr/2018
                    resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());
                    break;

                case DOCUMENT:
                    Exchange exchangeDocument = template.send("direct:FHIRBundleDocumentUpdate", ExchangePattern.InOut, new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            exchange = buildBundlePost(exchange,newXmlResource,conditional,"PUT");
                        }
                    });
                    // TODO need proper responses from the camel processor. KGM 18/Apr/2018

                    // This response is coming from an external FHIR Server, so uses inputstream
                    resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeDocument.getIn().getBody());


                default:
                    // TODO
            }
        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }


        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);


        return method;
    }


}
