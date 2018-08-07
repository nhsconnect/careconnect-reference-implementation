package uk.nhs.careconnect.ri.gatewaylib.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.ListResource;
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
public class ListResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(ListResourceProvider.class);

    @Override
    public Class<ListResource> getResourceType() {
        return ListResource.class;
    }

    
    @Read
    public ListResource getListById(HttpServletRequest httpRequest, @IdParam IdType internalId) {

        ProducerTemplate template = context.createProducerTemplate();


        ListResource form = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream)  template.sendBody("direct:FHIRList",
                    ExchangePattern.InOut,httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRList",ExchangePattern.InOut, new Processor() {
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
        if (resource instanceof ListResource) {
            form = (ListResource) resource;
        }else if (resource instanceof OperationOutcome)
        {

            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));

            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return form;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam ListResource form) {



        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(form);

            Exchange exchangeBundle = template.send("direct:FHIRList", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "List");
                }
            });

            // This response is coming from an external FHIR Server, so uses inputstream
            if (exchangeBundle.getIn().getBody() instanceof InputStream) {
                log.trace("RESPONSE InputStream");
                inputStream = (InputStream) exchangeBundle.getIn().getBody();
                Reader reader = new InputStreamReader(inputStream);
                resource = ctx.newXmlParser().parseResource(reader);
            } else
            if (exchangeBundle.getIn().getBody() instanceof String) {
                log.trace("RESPONSE String = "+(String) exchangeBundle.getIn().getBody());
                resource = ctx.newXmlParser().parseResource((String) exchangeBundle.getIn().getBody());
                log.trace("RETURNED String Resource "+resource.getClass().getSimpleName());
            } else {
                log.info("MESSAGE TYPE "+exchangeBundle.getIn().getBody().getClass());
            }

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof ListResource) {
            form = (ListResource) resource;
        } else if (resource instanceof OperationOutcome) {

            OperationOutcome operationOutcome =(OperationOutcome) resource;
            log.trace("OP OUTCOME PROCESS " + operationOutcome.getIssue().size() );
            if(operationOutcome.getIssue().size()>0)
            {
                log.info("Server Returned: "+operationOutcome.getIssueFirstRep().getDiagnostics());
                OperationOutcomeFactory.convertToException(operationOutcome);
            }
        }
        else {
            throw new InternalErrorException("Unknown Error");
        }

        MethodOutcome method = new MethodOutcome();

        if (resource instanceof OperationOutcome) {
            OperationOutcome opOutcome = (OperationOutcome) resource;
            method.setOperationOutcome(opOutcome);
            method.setCreated(false);
        } else {
            method.setCreated(true);
            OperationOutcome opOutcome = new OperationOutcome();
            method.setOperationOutcome(opOutcome);
            method.setId(resource.getIdElement());
            method.setResource(resource);
        }

        return method;
    }

    @Search
    public List<ListResource> searchListResource(HttpServletRequest httpRequest,
                                                                   @OptionalParam(name = ListResource.SP_IDENTIFIER) TokenParam identifier,
                                                                   @OptionalParam(name= ListResource.SP_RES_ID) TokenParam id,
                                                                   @OptionalParam(name = ListResource.SP_PATIENT) ReferenceParam patient
    ) {

        List<ListResource> results = new ArrayList<ListResource>();

        ProducerTemplate template = context.createProducerTemplate();



        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRList",
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
                ListResource form = (ListResource) entry.getResource();
                results.add(form);
            }
        } else if (resource instanceof OperationOutcome)
        {

            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));

            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Server Error",(OperationOutcome) resource);
        }

        return results;

    }


}
