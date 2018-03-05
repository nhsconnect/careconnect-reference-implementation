package uk.nhs.careconnect.ri.extranet.providers;


import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateRangeParam;
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
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class PatientExtranetProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    IPatient patientDao;

    @Autowired
    IComposition compositionDao;

    @Autowired
    CamelContext context;

    private static final Logger log = LoggerFactory.getLogger(PatientExtranetProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Search
    public List<Resource> searchPatient(HttpServletRequest request,

                                        @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,

                                        @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                        @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                        @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                        @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                        @OptionalParam(name= Patient.SP_NAME) StringParam name

            , @OptionalParam(name = Patient.SP_RES_ID) TokenParam resid

    ) {

        List<Resource> results = new ArrayList<>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRPatient",
                ExchangePattern.InOut,request);

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
            log.trace("Found Entries = " + bundle.getEntry().size());
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                //Patient patient = (Patient) ;
                results.add(entry.getResource());
            }
        } else if (resource instanceof OperationOutcome)
        {

            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));

            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Server Error",(OperationOutcome) resource);
        }
        return results;

    }

    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle patientDocumentOperation(
            @IdParam IdType patientId

    ) {
        log.info("In document " +patientId.getIdPart());

        HttpServletRequest request =  null;

        IGenericClient client = FhirContext.forDstu3().newRestfulGenericClient("http://purple.testlab.nhs.uk/careconnect-ri/STU3/");

        log.info("Build client");
        client.setEncoding(EncodingEnum.XML);

        log.info("calling composition");
        return compositionDao.buildSummaryCareDocument(client,patientId);

    }

     /*
    @Operation(name = "everything", idempotent = true, bundleType= BundleTypeEnum.SEARCHSET)
    public Bundle patientEverythingOperation(
            @IdParam IdType patientId
    ) {
        HttpServletRequest request =  null;
        CompleteBundle completeBundle = new CompleteBundle(practitionerProvider, organistionProvider, locationProvider);
        Bundle bundle = completeBundle.getBundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        Patient patient = getPatientById(request, patientId);
        if (patient !=null) {
            bundle.addEntry().setResource(patient);
            log.info("Practitioner");
            for (Reference gp : patient.getGeneralPractitioner()) {
                completeBundle.addGetPractitioner(new IdType(gp.getReference()));
            }
            Reference prac = patient.getManagingOrganization();
            log.info("Organization");
            if (prac!=null && prac.getReference() !=null) {
                completeBundle.addGetOrganisation(new IdType(prac.getReference()));
            }
        }
        // Populate bundle with matching resources
        log.info("Condition");
        conditionResourceProvider.conditionEverythingOperation(patientId,completeBundle);

        log.info("Observation");
        observationResourceProvider.observationEverythingOperation(patientId,completeBundle);

        log.info("Procedure");
        procedureResourceProvider.procedureEverythingOperation(patientId,completeBundle);

        log.info("AllergyIntolerance");
        allergyIntoleranceResourceProvider.getEverythingOperation(patientId,completeBundle);

        log.info("Encounter");
        encounterResourceProvider.getEverythingOperation(patientId,completeBundle);

        log.info("Immunization");
        immunizationResourceProvider.getEverythingOperation(patientId,completeBundle);

        log.info("medicationRequest");
        medicationRequestResourceProvider.getEverythingOperation(patientId,completeBundle);

        log.info("medicationStatement");
        medicationStatementResourceProvider.getEverythingOperation(patientId,completeBundle);

        return bundle;
    }
    */

}
