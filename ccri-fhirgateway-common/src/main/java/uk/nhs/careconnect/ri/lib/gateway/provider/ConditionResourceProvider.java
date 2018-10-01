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
public class ConditionResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(ConditionResourceProvider.class);
/*
    @Validate
    public MethodOutcome testResource(@ResourceParam Condition resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
*/
    @Override
    public Class<Condition> getResourceType() {
        return Condition.class;
    }
    
    public Bundle conditionEverythingOperation(
            @IdParam IdType patientId
            ,CompleteBundle completeBundle
    ) throws Exception {
        
        Bundle bundle = new Bundle();

        List<Condition> conditions = searchCondition(null, new ReferenceParam().setValue(patientId.getValue()),null,null,null,null);
      
        for (Condition condition : conditions) {
            if (condition.getAsserter() != null && condition.getAsserter().getReference() != null) {
                completeBundle.addGetPractitioner(new IdType(condition.getAsserter().getReference()));
            }
            bundle.addEntry().setResource(condition);
        }
        // Populate bundle with matching resources
        return bundle;
    }

    @Read
    public Condition getConditionById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        Condition condition = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRCondition",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Condition) {
            condition = (Condition) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return condition;
    }

    @Search
    public List<Condition> searchCondition(HttpServletRequest httpRequest,
                                           @OptionalParam(name = Condition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Condition.SP_CATEGORY) TokenParam category
            , @OptionalParam(name = Condition.SP_CLINICAL_STATUS) TokenParam clinicalstatus
            , @OptionalParam(name = Condition.SP_ASSERTED_DATE) DateRangeParam asserted
            , @OptionalParam(name = AllergyIntolerance.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<Condition> results = new ArrayList<Condition>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRCondition",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRCondition",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Condition");
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
                Condition condition = (Condition) entry.getResource();
                results.add(condition);
            }
        }
        else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
