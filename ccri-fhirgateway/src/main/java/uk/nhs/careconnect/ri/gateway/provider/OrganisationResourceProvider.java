package uk.nhs.careconnect.ri.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
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
public class OrganisationResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(OrganisationResourceProvider.class);

    @Override
    public Class<Organization> getResourceType() {
        return Organization.class;
    }



    @Read
    public Organization getOrganizationById(HttpServletRequest theRequest, @IdParam IdType internalId) {

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


        Organization patient = null;

        try {
            InputStream inputStream = (InputStream)  template.sendBodyAndHeaders("direct:FHIROrganization",
                    ExchangePattern.InOut,theRequest.getInputStream(), headerMap);
            log.info("Producer Return :" + inputStream);

            Reader reader = new InputStreamReader(inputStream);
            patient = ctx.newJsonParser().parseResource(Organization.class,reader);

        }
        catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
        }
        return patient;
    }

    @Search
    public List<Organization> searchOrganization(HttpServletRequest theRequest,
                                                 @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier,
                                                 @OptionalParam(name = Organization.SP_NAME) StringParam name
                                       ) {

        List<Organization> results = new ArrayList<Organization>();

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

        InputStream inputStream = (InputStream) template.sendBodyAndHeaders("direct:FHIROrganisation",
                ExchangePattern.InOut,"", headerMap);

        Bundle bundle = null;

        try {
            Reader reader = new InputStreamReader(inputStream);
            bundle = ctx.newJsonParser().parseResource(Bundle.class,reader);
            log.info("Found Entries = "+bundle.getEntry().size());
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Organization patient = (Organization) entry.getResource();
                results.add(patient);
            }
        }
        catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
        }

        return results;

    }



}
