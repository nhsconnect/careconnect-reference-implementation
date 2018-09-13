package uk.nhs.careconnect.ri.lib.gateway.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class ResourceTestProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(ResourceTestProvider.class);

    public MethodOutcome testResource(@ResourceParam IBaseResource resourceToValidate,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        Exchange exchange = template.send("direct:FHIRValidate", ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(ctx.newXmlParser().encodeResourceToString(resourceToValidate));
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, resourceToValidate.getClass().getSimpleName()+"/$validate");
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
            }
        });
        IBaseResource resource = null;
            if(exchange.getIn().

        getBody() instanceof InputStream)

        {
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            resource = null;
            try {
                resource = ctx.newXmlParser().parseResource(reader);
            } catch (Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
        }
            if(exchange.getIn().

        getBody() instanceof String)

        {
            resource = ctx.newXmlParser().parseResource((String) exchange.getIn().getBody());
        }

        MethodOutcome retVal = new MethodOutcome();

            if(resource instanceof OperationOutcome)

        {
            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Issue Count = " + operationOutcome.getIssue().size());
            retVal.setOperationOutcome(operationOutcome);
        } else

        {
            throw new InternalErrorException("Server Error", (OperationOutcome) resource);
        }

        return retVal;
}
}
