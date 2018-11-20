package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationAdministration;
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
public class MedicationAdministrationResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(MedicationAdministrationResourceProvider.class);

    @Override
    public Class<MedicationAdministration> getResourceType() {
        return MedicationAdministration.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam MedicationAdministration resource,
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

        List<MedicationAdministration> resources = searchMedicationAdministration(null, new ReferenceParam().setValue(patientId.getValue()),null,null,null, null);

        for (MedicationAdministration resource : resources) {
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
    public MedicationAdministration read(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        MedicationAdministration administration = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRMedicationAdministration",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof MedicationAdministration) {
            administration = (MedicationAdministration) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return administration;
    }

    @Search
    public List<MedicationAdministration> search(HttpServletRequest httpRequest,
                                                             @OptionalParam(name = MedicationAdministration.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationAdministration.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationAdministration.SP_RES_ID) StringParam id
            , @OptionalParam(name = MedicationAdministration.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationAdministration.SP_CODE) TokenParam code
            , @OptionalParam(name= MedicationAdministration.SP_MEDICATION) ReferenceParam medication
                                       ) throws Exception {

        List<MedicationAdministration> results = new ArrayList<MedicationAdministration>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRMedicationAdministration",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRMedicationAdministration",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationAdministration");
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
                MedicationAdministration administration = (MedicationAdministration) entry.getResource();
                results.add(administration);
            }

    } else {
        ProviderResponseLibrary.createException(ctx,resource);
    }

        return results;

    }



}
