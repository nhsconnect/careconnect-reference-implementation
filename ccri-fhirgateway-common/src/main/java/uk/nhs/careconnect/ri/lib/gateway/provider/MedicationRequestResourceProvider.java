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
public class MedicationRequestResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(MedicationRequestResourceProvider.class);

    @Override
    public Class<MedicationRequest> getResourceType() {
        return MedicationRequest.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam MedicationRequest resource,
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

        List<MedicationRequest> resources = searchMedicationRequest(null, new ReferenceParam().setValue(patientId.getValue()),null,null,null, null);

        for (MedicationRequest resource : resources) {
            if (resource.getRequester()!= null && resource.getRequester().hasAgent()) {
                Reference reference = resource.getRequester().getAgent();
                if (reference.getReference().contains("Practitioner")) {
                    completeBundle.addGetPractitioner(new IdType(reference.getReference()));
                }
                if (reference.getReference().contains("Organization")) {
                    completeBundle.addGetOrganisation(new IdType(reference.getReference()));
                }
            }
            bundle.addEntry().setResource(resource);
        }
        // Populate bundle with matching resources
        return bundle;
    }
*/
    @Read
    public MedicationRequest getMedicationRequestById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        MedicationRequest prescription = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRMedicationRequest",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof MedicationRequest) {
            prescription = (MedicationRequest) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return prescription;
    }

    @Search
    public List<MedicationRequest> searchMedicationRequest(HttpServletRequest httpRequest,
                                                           @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient
           // , @OptionalParam(name = MedicationRequest.SP_CODE) TokenParam code
            , @OptionalParam(name = MedicationRequest.SP_AUTHOREDON) DateRangeParam dateWritten
            , @OptionalParam(name = MedicationRequest.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationRequest.SP_RES_ID) StringParam resid
            , @OptionalParam(name= MedicationRequest.SP_MEDICATION) ReferenceParam medication
                                       ) throws Exception {

        List<MedicationRequest> results = new ArrayList<MedicationRequest>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRMedicationRequest",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRMedicationRequest",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationRequest");
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
                MedicationRequest prescription = (MedicationRequest) entry.getResource();
                results.add(prescription);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
