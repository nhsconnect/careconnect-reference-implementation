package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
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
public class ReferralRequestResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(ReferralRequestResourceProvider.class);

    @Autowired
    ResourceTestProvider resourceTestProvider;

    @Override
    public Class<ReferralRequest> getResourceType() {
        return ReferralRequest.class;
    }
    
    
    @Read
    public ReferralRequest getReferralRequestById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();

        ReferralRequest referralRequest = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRReferralRequest",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof ReferralRequest) {
            referralRequest = (ReferralRequest) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return referralRequest;
    }

    @Search
    public List<ReferralRequest> searchReferralRequest(HttpServletRequest httpRequest,
                                                       @OptionalParam(name = ReferralRequest.SP_IDENTIFIER) TokenParam identifier,
                                                    //   @OptionalParam(name = ReferralRequest.SP_TYPE) TokenOrListParam codes,
                                                       @OptionalParam(name = ReferralRequest.SP_RES_ID) StringParam id,
                                                       @OptionalParam(name = ReferralRequest.SP_PATIENT) ReferenceParam patient
                                       ) throws Exception {

        List<ReferralRequest> results = new ArrayList<ReferralRequest>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRReferralRequest",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRReferralRequest",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "ReferralRequest");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
        }
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
                ReferralRequest referralRequest = (ReferralRequest) entry.getResource();
                results.add(referralRequest);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
