package uk.nhs.careconnect.ri.lib.gateway.camel.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BinaryResource implements Processor {

    private FhirContext ctx;

    private String hapiBase;

    private Bundle bundle;

    private Exchange exchange;

    public BinaryResource(FhirContext ctx, String hapiBase) {
        this.ctx = ctx;
        this.hapiBase = hapiBase;
    }
    private static final Logger log = LoggerFactory.getLogger(BinaryResource.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        this.exchange = exchange;
        IBaseResource resource = ctx.newXmlParser().parseResource((String) exchange.getIn().getBody());

        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;

            for (Bundle.BundleEntryComponent bundleEntry : bundle.getEntry()) {
                //log.info(bundleEntry.getResource().getClass().getCanonicalName());
                if (bundleEntry.getResource() instanceof DocumentReference) {
                    log.debug("FOUND DocumentReference");
                    // Document Reference is present, so post included Binary (/TODO Binaries)
                    DocumentReference documentReference = (DocumentReference) bundleEntry.getResource();

                    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                        if (entry.getResource() instanceof Binary) {

                            Binary binary = (Binary) entry.getResource();
                            log.debug("Binary fullUrl = "+entry.getFullUrl() + " Binary.id = "+binary.getId());

                            for (DocumentReference.DocumentReferenceContentComponent contentComponent : documentReference.getContent()) {
                                // url of the DocumentReference and FullUrl of the entry(Binary) should match
                                log.debug("DocumentReference content attachment refUrl = "+contentComponent.getAttachment().getUrl());
                                if (contentComponent.hasAttachment() && contentComponent.getAttachment().getUrl().equals(entry.getFullUrl())) {
                                    log.debug("FOUND Binary");
                                    postBinaryResource(binary,contentComponent);
                                }
                            }
                        }
                    }
                }
                String newBundle = ctx.newXmlParser().encodeResourceToString(bundle);
                exchange.getIn().setBody(newBundle);
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            }
        }

    }
    private void postBinaryResource(Binary binary, DocumentReference.DocumentReferenceContentComponent contentComponent) {
        ProducerTemplate template = exchange.getContext().createProducerTemplate();
        String jsonResource = ctx.newXmlParser().encodeResourceToString(binary);
        try {
            Exchange edmsExchange = template.send("direct:FHIRBinary", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Binary");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setBody(jsonResource);
                }
            });

            if (edmsExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE) != null && (edmsExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE).toString().equals("201"))) {
                // Now update the document links
                String[] path = edmsExchange.getIn().getHeader("Location").toString().split("/");
                String resourceId = path[path.length - 1];
                log.info("Binary resource Id = " + resourceId);
                contentComponent.getAttachment().setUrl(hapiBase + "/Binary/" + resourceId);
            }
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }

    }
}
