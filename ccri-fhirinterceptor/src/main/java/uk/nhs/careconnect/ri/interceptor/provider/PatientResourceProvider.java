package uk.nhs.careconnect.ri.interceptor.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Component
public class PatientResourceProvider implements IResourceProvider {


    FhirContext ctx = FhirContext.forDstu3();

    @Autowired
    CamelContext context;

    private static final List<String> MANDATORY_PARAM_NAMES = Arrays.asList("patientNHSNumber", "recordSection");
    private static final List<String> PERMITTED_PARAM_NAMES = new ArrayList<String>(MANDATORY_PARAM_NAMES) {{
        add("timePeriod");
    }};


    private static final Logger log = LoggerFactory.getLogger(PatientResourceProvider.class);



    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }



    @Read
    public Patient getPatientById(HttpServletRequest theRequest, @IdParam IdType internalId) {

        log.info("Request:"+ theRequest.getRequestURI());
        return null;
    }

    @Search
    public List<Patient> searchPatient(HttpServletRequest theRequest,

                                       @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                                       @OptionalParam(name= Patient.SP_BIRTHDATE) DateParam birthDate,
                                       @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                                       @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                       @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                       @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                       @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                       @OptionalParam(name= Patient.SP_NAME) StringParam name,
                                       @OptionalParam(name= Patient.SP_PHONE) StringParam phone
                                       ) {

        List<Patient> results = new ArrayList<Patient>();
        log.info("Request:"+ theRequest.getRequestURI());

        log.info("Request Query String:"+ theRequest.getQueryString());

        ProducerTemplate template = context.createProducerTemplate();

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(Exchange.HTTP_METHOD, theRequest.getMethod());
        headerMap.put(Exchange.HTTP_QUERY, theRequest.getQueryString());
        headerMap.put(Exchange.HTTP_PATH, "Patient");
        headerMap.put(Exchange.ACCEPT_CONTENT_TYPE, "application/json");

        InputStream inputStream = (InputStream) template.sendBodyAndHeaders("direct:FHIRServer",
                ExchangePattern.InOut,"", headerMap);

        Bundle bundle = null;

        try {
            Reader reader = new InputStreamReader(inputStream);
            bundle = ctx.newJsonParser().parseResource(Bundle.class,reader);
            log.info("Found Entries = "+bundle.getEntry().size());
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                results.add((Patient) entry.getResource());
            }
        }
        catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
        }

        return results;

    }



}
