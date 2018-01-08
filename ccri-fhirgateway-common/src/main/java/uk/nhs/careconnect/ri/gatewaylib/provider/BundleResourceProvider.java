package uk.nhs.careconnect.ri.gatewaylib.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
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
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class BundleResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(BundleResourceProvider.class);

    @Override
    public Class<Bundle> getResourceType() {
        return Bundle.class;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Bundle bundle) {


        // Example message https://gist.github.com/IOPS-DEV/1a532eb43b226dcd6ce26a6b698019f4#file-ec_edischarge_full_payload_example-01


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);

        // Process resources
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();

            // Look for existing resources. Ideally we should not need to add Patient, Practitioner, Organization, etc
            // These should be using well known identifiers and ideally will be present on our system.

            if (resource instanceof Organization ) {
                searchAddOrganisation((Organization) resource);
            } else
            if (resource instanceof Practitioner ) {
                searchAddPractitioner((Practitioner) resource);
            } else
            if (resource instanceof Patient ) {
                searchAddPatient((Patient) resource);
            } else {
                log.info("Not searched for "+resource.getClass());
            }
        }

        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        return method;
    }

    public Practitioner searchAddPractitioner(Practitioner practitioner) {
        Practitioner returnedPractioner = null;

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : practitioner.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRPractitioner", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Practitioner");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size() > 0) {
                    Practitioner EPRpractitioner = (Practitioner) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Practitioner = " + EPRpractitioner.getId());
                }
            }
        }

        return returnedPractioner;
    }

    public Organization searchAddOrganisation(Organization organisation) {
        Organization returnedOrganization  = null;

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : organisation.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIROrganisation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Organization");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    Organization EPRorganisation = (Organization) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Organization = " + EPRorganisation.getId());
                }
            }
        }


        return returnedOrganization;
    }

    public Patient searchAddPatient(Patient patient) {
        Patient returnedPatient  = null;
        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : patient.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRPatient", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size() > 0) {
                    Patient EPRpatient = (Patient) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Patient = " + EPRpatient.getId());
                }
            }
        }
        return returnedPatient;
    }

}
