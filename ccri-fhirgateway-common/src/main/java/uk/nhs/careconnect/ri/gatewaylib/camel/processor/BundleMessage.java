package uk.nhs.careconnect.ri.gatewaylib.camel.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        this.context = exchange.getContext();

        resourceMap = new HashMap<>();

        String bundleString = exchange.getIn().getBody().toString();

        IParser parser = ctx.newXmlParser();
        bundle = parser.parseResource(Bundle.class,bundleString);

        BundleCore bundleCore = new BundleCore(ctx,context,bundle);

        // Process resources
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();

            // Look for existing resources. Ideally we should not need to add Patient, Practitioner, Organization, etc
            // These should be using well known identifiers and ideally will be present on our system.

            if (resource.getId() != null) {
                bundleCore.searchAddResource(resource.getId());
            } else {
                resource.setId(java.util.UUID.randomUUID().toString());
                bundleCore.searchAddResource(resource.getId());
            }

        }

    }


}
