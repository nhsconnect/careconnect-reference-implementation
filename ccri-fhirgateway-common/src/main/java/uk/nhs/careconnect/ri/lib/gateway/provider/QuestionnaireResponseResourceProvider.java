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

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuestionnaireResponseResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireResponseResourceProvider.class);

    @Override
    public Class<QuestionnaireResponse> getResourceType() {
        return QuestionnaireResponse.class;
    }

    
    @Read
    public QuestionnaireResponse getQuestionnaireById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();


        QuestionnaireResponse form = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream)  template.sendBody("direct:FHIRQuestionnaireResponse",
                    ExchangePattern.InOut,httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRQuestionnaireResponse",ExchangePattern.InOut, new Processor() {
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
        if (resource instanceof QuestionnaireResponse) {
            form = (QuestionnaireResponse) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return form;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam QuestionnaireResponse form) throws Exception {



        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {

            String newXmlResource = ctx.newXmlParser().encodeResourceToString(form);

            Exchange exchangeBundle = template.send("direct:FHIRQuestionnaireResponse", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "QuestionnaireResponse");
                }
            });

            // This response is coming from an external FHIR Server, so uses inputstream
            resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof org.hl7.fhir.dstu3.model.QuestionnaireResponse) {
            form = (QuestionnaireResponse) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);
        return method;
    }

    @Search
    public List<QuestionnaireResponse> searchQuestionnaireResponse(HttpServletRequest httpRequest,
                                                                   @OptionalParam(name = QuestionnaireResponse.SP_IDENTIFIER) TokenParam identifier,
                                                                   @OptionalParam(name= QuestionnaireResponse.SP_RES_ID) StringParam id,
                                                                   @OptionalParam(name= QuestionnaireResponse.SP_QUESTIONNAIRE) ReferenceParam questionnaire,
                                                                   @OptionalParam(name = QuestionnaireResponse.SP_PATIENT) ReferenceParam patient
    ) throws Exception {

        List<QuestionnaireResponse> results = new ArrayList<QuestionnaireResponse>();

        ProducerTemplate template = context.createProducerTemplate();



        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRQuestionnaireResponse",
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
                QuestionnaireResponse form = (QuestionnaireResponse) entry.getResource();
                results.add(form);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        return results;

    }


}
