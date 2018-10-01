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
public class ProcedureResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(ProcedureResourceProvider.class);

    @Autowired
    ResourceTestProvider resourceTestProvider;

    @Override
    public Class<Procedure> getResourceType() {
        return Procedure.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam Procedure resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    */
    public Bundle procedureEverythingOperation(
            @IdParam IdType patientId
            ,CompleteBundle completeBundle
    ) throws Exception {

        Bundle bundle = completeBundle.getBundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        List<Procedure> resources = searchProcedure(null, new ReferenceParam().setValue(patientId.getValue()),null, null,null);

        for (Procedure resource : resources) {
            for (Procedure.ProcedurePerformerComponent performerComponent : resource.getPerformer()) {
                Reference reference = performerComponent.getActor();
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

    @Read
    public Procedure getProcedureById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();

        Procedure procedure = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRProcedure",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Procedure) {
            procedure = (Procedure) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return procedure;
    }

    @Search
    public List<Procedure> searchProcedure(HttpServletRequest httpRequest,
                                           @OptionalParam(name = Procedure.SP_PATIENT) ReferenceParam patient
             ,@OptionalParam(name = Procedure.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Procedure.SP_SUBJECT) ReferenceParam subject
            , @OptionalParam(name = Procedure.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<Procedure> results = new ArrayList<Procedure>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRProcedure",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRProcedure",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Procedure");
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
                Procedure procedure = (Procedure) entry.getResource();
                results.add(procedure);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
