package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;

import org.hl7.fhir.dstu3.model.Binary;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@Component
public class BinaryResourceProvider implements IResourceProvider {



    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(BinaryResourceProvider.class);


    @Override
    public Class<Binary> getResourceType() {
        return Binary.class;
    }

  
    @Read
    public Binary getBinaryById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {


        // We get raw response from FHIR Server. Need to convert back to Binary for HAPI Handling
        ProducerTemplate template = context.createProducerTemplate();


        Binary binary = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;

            Exchange exchange = template.send("direct:FHIRBinary",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, internalId.getValue());
                }
            });
            binary= new Binary();

            binary.setContentType(exchange.getIn().getHeader(Exchange.CONTENT_TYPE).toString());
            log.trace("Return Content-Type = "+binary.getContentType());
            log.trace("Response Code "+exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE));

            if (exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE).equals(200)) {

                binary.setId(internalId.getValue());
                if (binary.getContentType().equals("application/fhir+xml")) {
                    // This server defaults to JSON so convert content to json
                    binary.setContentType("application/fhir+json");

                    inputStream = (InputStream) exchange.getIn().getBody();
                    Reader reader = new InputStreamReader(inputStream);
                    // read the resource
                    IBaseResource resourceT = ctx.newXmlParser().parseResource(reader);

                    String jsonResource = ctx.newJsonParser().encodeResourceToString(resourceT);
                    binary.setContent(jsonResource.getBytes());

                } else {
                    //log.info("Content type not application/fhir");

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    try {
                        org.apache.commons.io.IOUtils.copyLarge((InputStream) exchange.getIn().getBody(), buffer);
                        byte[] byteArray = buffer.toByteArray();
                        binary.setContent(byteArray);
                    } catch (IOException ex) {
                        log.error("Processing returned Binary:" + ex.getMessage());
                        binary = null;
                    }
                }

                if (binary == null) {
                    log.warn("Binary is now null");
                    inputStream = (InputStream) exchange.getIn().getBody();
                    Reader reader = new InputStreamReader(inputStream);
                    resource = ctx.newJsonParser().parseResource(reader);
                }
            } else {
                inputStream = (InputStream) exchange.getIn().getBody();
                Reader reader = new InputStreamReader(inputStream);

                if (binary.getContentType().equals("application/fhir+xml")) {


                    resource = ctx.newXmlParser().parseResource(reader);
                } else {
                    resource = ctx.newJsonParser().parseResource(reader);
                }
                if (resource instanceof OperationOutcome) {
                    OperationOutcome outcome = (OperationOutcome) resource;
                    if (outcome.getIssue().size() > 0 && exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE).equals(404) ) {
                        outcome.getIssue().get(0).setCode(OperationOutcome.IssueType.NOTFOUND);
                    }
                    OperationOutcomeFactory.convertToException(outcome);
                }
            }


        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }

        if (resource !=null) {
            log.warn("Unexpected resource != null "+resource.getClass().getCanonicalName());

            ProviderResponseLibrary.createException(ctx,resource);

        }


        return binary;
    }





}
