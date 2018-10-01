package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
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
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class AllergyIntoleranceResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(AllergyIntoleranceResourceProvider.class);

    @Override
    public Class<AllergyIntolerance> getResourceType() {
        return AllergyIntolerance.class;
    }

    @Autowired
    ResourceTestProvider resourceTestProvider;
/*
    @Validate
    public MethodOutcome testResource(@ResourceParam AllergyIntolerance resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

* /
    /*
    public Bundle getEverythingOperation(
            @IdParam IdType patientId
            ,CompleteBundle completeBundle
    ) {

        Bundle bundle =completeBundle.getBundle();

        List<AllergyIntolerance> resources = searchAllergyIntolerance(null, new ReferenceParam().setValue(patientId.getValue()),null,null,null);

        for (AllergyIntolerance resource : resources) {
            if (resource.getAsserter() != null && resource.getAsserter().getReference() != null) {
                completeBundle.addGetPractitioner(new IdType(resource.getAsserter().getReference()));
            }
            bundle.addEntry().setResource(resource);
        }
        // Populate bundle with matching resources
        return bundle;
    }
*/

    @Read
    public AllergyIntolerance getAllergyIntoleranceById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        AllergyIntolerance allergy = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRAllergyIntolerance",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof AllergyIntolerance) {
            allergy = (AllergyIntolerance) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return allergy;
    }

    @Search
    public List<AllergyIntolerance> searchAllergyIntolerance(HttpServletRequest httpRequest,
                                                             @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus
            , @OptionalParam(name = AllergyIntolerance.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<AllergyIntolerance> results = new ArrayList<AllergyIntolerance>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRAllergyIntolerance",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRAllergyIntolerance",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "AllergyIntolerance");
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
                AllergyIntolerance allergy = (AllergyIntolerance) entry.getResource();
                results.add(allergy);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
