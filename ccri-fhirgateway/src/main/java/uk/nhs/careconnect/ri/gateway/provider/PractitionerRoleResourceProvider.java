package uk.nhs.careconnect.ri.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PractitionerRoleResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PractitionerRoleResourceProvider.class);

    @Override
    public Class<PractitionerRole> getResourceType() {
        return PractitionerRole.class;
    }



    @Read
    public PractitionerRole getPractitionerRoleById(HttpServletRequest theRequest, @IdParam IdType internalId) {

        ProducerTemplate template = context.createProducerTemplate();

        Map<String, Object> headerMap = new HashMap<>();

        headerMap.put(Exchange.HTTP_METHOD, theRequest.getMethod());

        if (theRequest.getQueryString() != null) {
            headerMap.put(Exchange.HTTP_QUERY, theRequest.getQueryString().replace("format=xml","format=json"));
        } else {
            headerMap.put(Exchange.HTTP_QUERY,null);
        }

        //headerMap.put(Exchange.HTTP_QUERY, theRequest.getQueryString());
        headerMap.put(Exchange.HTTP_PATH, theRequest.getPathInfo());
        headerMap.put(Exchange.ACCEPT_CONTENT_TYPE, "application/json");


        PractitionerRole practitionerRole = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBodyAndHeaders("direct:FHIRPractitionerRole",
                    ExchangePattern.InOut,theRequest.getInputStream(), headerMap);

            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);

        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof PractitionerRole) {
            practitionerRole = (PractitionerRole) resource;
        }
        else {
            throw new InternalErrorException("Server Error",(OperationOutcome) resource);
        }
        return practitionerRole;
    }

    @Search
    public List<PractitionerRole> searchPractitionerRole(HttpServletRequest theRequest,
                                                         @OptionalParam(name = PractitionerRole.SP_PRACTITIONER) ReferenceParam practitioner,
                                                         @OptionalParam(name = PractitionerRole.SP_ORGANIZATION) ReferenceParam organisation
                                       ) {

        List<PractitionerRole> results = new ArrayList<PractitionerRole>();

        ProducerTemplate template = context.createProducerTemplate();

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(Exchange.HTTP_METHOD, theRequest.getMethod());
        if (theRequest.getQueryString() != null) {
            headerMap.put(Exchange.HTTP_QUERY, theRequest.getQueryString().replace("format=xml","format=json"));
        } else {
            headerMap.put(Exchange.HTTP_QUERY,null);
        }

        headerMap.put(Exchange.HTTP_PATH,  theRequest.getPathInfo());
        headerMap.put(Exchange.ACCEPT_CONTENT_TYPE, "application/json");

        InputStream inputStream = (InputStream) template.sendBodyAndHeaders("direct:FHIRPractitionerRole",
                ExchangePattern.InOut,"", headerMap);

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
                PractitionerRole patient = (PractitionerRole) entry.getResource();
                results.add(patient);
            }
        }
        else {
            throw new InternalErrorException("Server Error",(OperationOutcome) resource);
        }
        return results;

    }



}
