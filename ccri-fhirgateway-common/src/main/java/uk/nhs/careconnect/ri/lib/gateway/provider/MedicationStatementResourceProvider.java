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
public class MedicationStatementResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(MedicationStatementResourceProvider.class);

    @Override
    public Class<MedicationStatement> getResourceType() {
        return MedicationStatement.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam MedicationStatement resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    */
/*
    public Bundle getEverythingOperation(
            @IdParam IdType patientId
            ,CompleteBundle completeBundle
    ) {

        Bundle bundle = completeBundle.getBundle();

        List<MedicationStatement> resources = searchMedicationStatement(null, new ReferenceParam().setValue(patientId.getValue()),null,null,null);

        for (MedicationStatement resource : resources) {

            bundle.addEntry().setResource(resource);
        }
        // Populate bundle with matching resources
        return bundle;
    }
*/
    @Read
    public MedicationStatement getMedicationStatementById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        MedicationStatement statement = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRMedicationStatement",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof MedicationStatement) {
            statement = (MedicationStatement) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return statement;
    }

    @Search
    public List<MedicationStatement> searchMedicationStatement(HttpServletRequest httpRequest,
                                                               @OptionalParam(name = MedicationStatement.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationStatement.SP_EFFECTIVE) DateRangeParam effectiveDate
            , @OptionalParam(name = MedicationStatement.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationStatement.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<MedicationStatement> results = new ArrayList<MedicationStatement>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRMedicationStatement",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRMedicationStatement",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationStatement");
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
                MedicationStatement statement = (MedicationStatement) entry.getResource();
                results.add(statement);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
