package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
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
public class PractitionerRoleResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(PractitionerRoleResourceProvider.class);

    @Override
    public Class<PractitionerRole> getResourceType() {
        return PractitionerRole.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam PractitionerRole resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
*/

    @Read
    public PractitionerRole getPractitionerRoleById(HttpServletRequest theRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();

        PractitionerRole practitionerRole = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRPractitionerRole",
                    ExchangePattern.InOut,theRequest);

            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);

        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof PractitionerRole) {
            practitionerRole = (PractitionerRole) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return practitionerRole;
    }

    @Search
    public List<PractitionerRole> searchPractitionerRole(HttpServletRequest theRequest,
                                                         @OptionalParam(name = PractitionerRole.SP_PRACTITIONER) ReferenceParam practitioner,
                                                         @OptionalParam(name = PractitionerRole.SP_ORGANIZATION) ReferenceParam organisation
                                                         ,@OptionalParam(name = PractitionerRole.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = PractitionerRole.SP_RES_ID) StringParam resid
                                       )  throws Exception {

        List<PractitionerRole> results = new ArrayList<PractitionerRole>();

        ProducerTemplate template = context.createProducerTemplate();



        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRPractitionerRole",
                ExchangePattern.InOut,theRequest);

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
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        return results;

    }



}
