package uk.nhs.careconnect.ri.lib.gateway.camel.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import java.util.HashMap;
import java.util.Map;

public class BundleMessage implements Processor {

    public BundleMessage(FhirContext ctx) {
        this.ctx = ctx;
    }

    CamelContext context;

    FhirContext ctx;
    private Map<String,Resource> resourceMap;

    private Bundle bundle;

    private static final Logger log = LoggerFactory.getLogger(BundleMessage.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        // Bundles should be in XML format. Previous step should enforce this.

        log.info("Starting Message Bundle Processing");
        this.context = exchange.getContext();

        resourceMap = new HashMap<>();

        String bundleString = exchange.getIn().getBody().toString();

        IParser parser = ctx.newXmlParser();
        bundle = parser.parseResource(Bundle.class,bundleString);
        BundleCore bundleCore = new BundleCore(ctx,context,bundle);
        try {


            // Process resources
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Resource resource = entry.getResource();

                // Look for existing resources. Ideally we should not need to add Patient, Practitioner, Organization, etc
                // These should be using well known identifiers and ideally will be present on our system.

                if (resource.getId() != null) {
                    Resource resourceSearch = bundleCore.searchAddResource(resource.getId());
                    if (resourceSearch instanceof OperationOutcome) {
                        setExchange(exchange,(OperationOutcome) resourceSearch);
                    }
                } else {
                    resource.setId(java.util.UUID.randomUUID().toString());
                    Resource resourceSearch = bundleCore.searchAddResource(resource.getId());
                    if (resourceSearch instanceof OperationOutcome) {
                        setExchange(exchange,(OperationOutcome) resourceSearch);
                    }
                }
                if (resource instanceof DocumentReference) {
                    log.info("Document Reference Location " + resource.getId());
                    exchange.getIn().setHeader("Location",resource.getId());
                    exchange.getIn().setHeader("Content-Location",resource.getId());
                }

            }
            exchange.getIn().setBody(ctx.newXmlParser().encodeResourceToString(bundleCore.getUpdatedBundle()));
            //log.info(ctx.newXmlParser().encodeResourceToString(bundleCore.getBundle()));

        } catch (Exception ex) {
            // A number of the HAPI related function will return exceptions.
            // Convert to operational outcomes
            String errorMessage;
            if (ex.getMessage()!= null) {
                errorMessage = ex.getMessage();
            } else {
                errorMessage = "BundleMessage Exception"+ex.getClass().getSimpleName();
            }
            log.error(errorMessage);
            OperationOutcome operationOutcome = null;
            if (bundleCore != null && bundleCore.getOperationOutcome() != null) {
                operationOutcome = bundleCore.getOperationOutcome();
            } else {
                operationOutcome=new OperationOutcome();
                OperationOutcome.IssueType issueType = OperationOutcomeFactory.getIssueType(ex);

                operationOutcome.addIssue()
                        .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                        .setCode(issueType)
                        .setDiagnostics(errorMessage);
            }

            setExchange(exchange,operationOutcome);
        }
        log.info("Finishing Message Bundle Processing");

    }

    private void setExchange(Exchange exchange, OperationOutcome operationOutcome) {
        exchange.getIn().setBody(ctx.newXmlParser().encodeResourceToString(operationOutcome));
    }



}
