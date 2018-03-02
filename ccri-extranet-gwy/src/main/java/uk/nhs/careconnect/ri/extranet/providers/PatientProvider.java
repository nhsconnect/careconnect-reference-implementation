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

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.extranet.IComposition;
import uk.nhs.careconnect.ri.extranet.IPatient;

import javax.servlet.http.HttpServletRequest;


import java.util.List;

@Component
public class PatientProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    IPatient patientDao;

    @Autowired
    IComposition compositionDao;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

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

        List<Resource> results = patientDao.search(ctx,birthDate,familyName,gender,givenName,identifier,name);


        return results;

    }

    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle compositionDocumentOperation(
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
