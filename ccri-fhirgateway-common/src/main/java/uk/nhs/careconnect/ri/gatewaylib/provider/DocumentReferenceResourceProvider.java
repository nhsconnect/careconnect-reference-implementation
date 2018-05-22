package uk.nhs.careconnect.ri.gatewaylib.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
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
import org.w3c.dom.Document;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.activation.UnsupportedDataTypeException;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class DocumentReferenceResourceProvider implements IResourceProvider {



    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ValidationProvider val;

    private static final Logger log = LoggerFactory.getLogger(DocumentReferenceResourceProvider.class);


    @Override
    public Class<DocumentReference> getResourceType() {
        return DocumentReference.class;
    }

    @Validate
    public MethodOutcome validate(@ResourceParam DocumentReference resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return val.validate(resource,theMode,theProfile);
    }
    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam DocumentReference documentReference) {

        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newJsonResource = ctx.newJsonParser().encodeResourceToString(documentReference);
            Exchange exchange = template.send("direct:FHIRDocumentReference",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newJsonResource);
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH,  "DocumentReference");
                    exchange.getIn().setHeader("Prefer","return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof DocumentReference) {
            documentReference = (DocumentReference) resource;
        }else if (resource instanceof OperationOutcome)
        {

            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));

            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);

        method.setId(documentReference.getIdElement());
        method.setResource(documentReference);

        return method;

    }


    @Read
    public DocumentReference getDocumentReferenceById(HttpServletRequest httpRequest, @IdParam IdType internalId) {

        ProducerTemplate template = context.createProducerTemplate();


        DocumentReference documentReference = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream)  template.sendBody("direct:FHIRDocumentReference",
                        ExchangePattern.InOut,httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRDocumentReference",ExchangePattern.InOut, new Processor() {
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
        if (resource instanceof DocumentReference) {
            documentReference = (DocumentReference) resource;
        }else if (resource instanceof OperationOutcome)
        {

            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));

            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }


        return documentReference;
    }

    @Search
    public List<DocumentReference> searchDocumentReference(HttpServletRequest httpRequest
            , @OptionalParam(name = DocumentReference.SP_RES_ID) TokenParam resid
            , @OptionalParam(name = DocumentReference.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = DocumentReference.SP_CREATED) DateRangeParam date
            , @OptionalParam(name = DocumentReference.SP_TYPE) TokenParam type
            , @OptionalParam(name = DocumentReference.SP_PERIOD)DateRangeParam dateRange
            , @OptionalParam(name = DocumentReference.SP_SETTING) TokenParam setting
    ) {



        List<DocumentReference> results = new ArrayList<DocumentReference>();

        ProducerTemplate template = context.createProducerTemplate();



        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRDocumentReference",
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
                DocumentReference documentReference = (DocumentReference) entry.getResource();
                results.add(documentReference);
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
