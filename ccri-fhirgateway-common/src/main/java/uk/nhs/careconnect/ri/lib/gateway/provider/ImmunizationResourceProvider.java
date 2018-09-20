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
public class ImmunizationResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(ImmunizationResourceProvider.class);

    @Override
    public Class<Immunization> getResourceType() {
        return Immunization.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam Immunization resource,
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

        List<Immunization> resources = searchImmunization(null, new ReferenceParam().setValue(patientId.getValue()),null,null,null);

        for (Immunization resource : resources) {
            if (resource.getLocation()!=null) {
                completeBundle.addGetLocation(new IdType(resource.getLocation().getReference()));
            }
            bundle.addEntry().setResource(resource);
        }
        // Populate bundle with matching resources
        return bundle;
    }
*/
    @Read
    public Immunization getImmunizationById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        Immunization immunization = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRImmunization",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Immunization) {
            immunization = (Immunization) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return immunization;
    }

    @Search
    public List<Immunization> searchImmunization(HttpServletRequest httpRequest,
                                                 @OptionalParam(name = Immunization.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Immunization.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Immunization.SP_STATUS) TokenParam status
            , @OptionalParam(name = Immunization.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<Immunization> results = new ArrayList<Immunization>();

        ProducerTemplate template = context.createProducerTemplate();
        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRImmunization",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRImmunization",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Immunization");
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
                Immunization immunization = (Immunization) entry.getResource();
                results.add(immunization);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
