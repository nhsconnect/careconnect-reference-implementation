package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentReferenceResourceProvider implements IResourceProvider {



    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    @Value("${ccri.server.base}")
    String serverBase;

    private static final Logger log = LoggerFactory.getLogger(DocumentReferenceResourceProvider.class);


    @Override
    public Class<DocumentReference> getResourceType() {
        return DocumentReference.class;
    }
/*
    @Validate
    public MethodOutcome testResource(@ResourceParam DocumentReference resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    */

    private DocumentReference convertFromSmartUrl(DocumentReference documentReference) {
    if (serverBase != null && serverBase.contains("ccri-fhir")) {
        for (DocumentReference.DocumentReferenceContentComponent context: documentReference.getContent()) {
            if (context.hasAttachment() && context.getAttachment().hasUrl()) {
                context.getAttachment().setUrl(context.getAttachment().getUrl().replace("ccri-smartonfhir","ccri-fhir"));
            }
        }
    }
    return documentReference;
}
    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam DocumentReference documentReference) throws Exception {

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
            documentReference = convertFromSmartUrl((DocumentReference) resource);

        } else {
            ProviderResponseLibrary.createException(ctx,resource);
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
    public DocumentReference getDocumentReferenceById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

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

            documentReference = convertFromSmartUrl((DocumentReference) resource);
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }


        return documentReference;
    }

    @Search
    public List<Resource> searchDocumentReference(HttpServletRequest httpRequest
            , @OptionalParam(name = DocumentReference.SP_RES_ID) StringParam resid
            , @OptionalParam(name = DocumentReference.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = DocumentReference.SP_CREATED) DateRangeParam date
            , @OptionalParam(name = DocumentReference.SP_TYPE) TokenParam type
            , @OptionalParam(name = DocumentReference.SP_PERIOD)DateRangeParam dateRange
            , @OptionalParam(name = DocumentReference.SP_SETTING) TokenParam setting
    ) throws Exception {



        List<Resource> results = new ArrayList<>();

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
                if (entry.getResource() instanceof DocumentReference) {
                    DocumentReference documentReference = convertFromSmartUrl((DocumentReference) entry.getResource());
                    results.add(documentReference);
                } else {
                    results.add(entry.getResource());
                }
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }




}
