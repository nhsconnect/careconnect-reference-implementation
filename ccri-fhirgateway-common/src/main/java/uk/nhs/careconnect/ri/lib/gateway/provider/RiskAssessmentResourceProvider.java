package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.ReferenceParam;
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
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class RiskAssessmentResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentResourceProvider.class);

    @Override
    public Class<RiskAssessment> getResourceType() {
        return RiskAssessment.class;
    }


   
    @Read
    public RiskAssessment getRiskAssessmentById(HttpServletRequest httpRequest, @IdParam IdType internalId) {

        ProducerTemplate template = context.createProducerTemplate();


        RiskAssessment carePlan = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream)  template.sendBody("direct:FHIRRiskAssessment",
                    ExchangePattern.InOut,httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRRiskAssessment",ExchangePattern.InOut, new Processor() {
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
        if (resource instanceof RiskAssessment) {
            carePlan = (RiskAssessment) resource;
        }else if (resource instanceof OperationOutcome)
        {

            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));

            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return carePlan;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam RiskAssessment riskAssessment) throws Exception {



        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(riskAssessment);

            Exchange exchangeBundle = template.send("direct:FHIRRiskAssessment", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "RiskAssessment");
                }
            });

            resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof RiskAssessment) {
            riskAssessment = (RiskAssessment) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();
        ProviderResponseLibrary.setMethodOutcome(resource,method);

        return method;
    }

    @Search
    public List<Resource> searchRiskAssessment(HttpServletRequest httpRequest,
                                         @OptionalParam(name = RiskAssessment.SP_PATIENT) ReferenceParam patient
                                        ,@OptionalParam(name = RiskAssessment.SP_IDENTIFIER) TokenParam identifier
                                        ,@OptionalParam(name = RiskAssessment.SP_RES_ID) StringParam id
    ) throws Exception {

        List<Resource> results = new ArrayList<>();

        ProducerTemplate template = context.createProducerTemplate();



        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRRiskAssessment",
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
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }


}
