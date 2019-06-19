package uk.nhs.careconnect.ccri.fhirserver.interceptor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.rest.server.method.ResourceParameter;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.*;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;


public class CCRequestValidatingInterceptor extends InterceptorAdapter {
    private Logger log = LoggerFactory.getLogger(CCRequestValidatingInterceptor.class);
    private String myErrorMessageFormat = "ERROR - ${operationType} - ${idOrResourceName}";

    private FhirValidator fhirValidator;

    ValidationResult results;

    FhirContext ctx;

    public CCRequestValidatingInterceptor(Logger ourLog, FhirValidator fhirValidator, FhirContext ctx) {
        super();
        //this.log = ourLog;
        this.fhirValidator = fhirValidator;

        this.ctx = ctx;
    }

    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
        EncodingEnum encoding = RestfulServerUtils.determineRequestEncodingNoDefault(theRequestDetails);
        //log.info("CC_VALIDATOR" + this.fhirValidator.toString());
        if (encoding == null) {
            log.trace("Incoming request does not appear to be FHIR, not going to validate");
            return true;
        } else {
            Charset charset = ResourceParameter.determineRequestCharset(theRequestDetails);
            String requestText = new String(theRequestDetails.loadRequestContents(), charset);
            if (StringUtils.isBlank(requestText)) {
                log.trace("Incoming request does not have a body");
                return true;
            } else {
                //log.info(theRequest.getMethod());
                if ((theRequest.getMethod().equals("POST") && !theRequest.getRequestURI().contains("$validate") ) || theRequest.getMethod().equals("PUT")) {


                    IBaseResource resource = null;
                    switch (encoding) {
                        case JSON:
                            resource = ctx.newJsonParser().parseResource(requestText);
                            break;
                        case XML:
                            resource = ctx.newXmlParser().parseResource(requestText);
                            break;
                    }
                    if (resource instanceof Bundle) {
                        Bundle bundle = (Bundle) resource;
                        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                           entry.setResource((Resource) setProfile(entry.getResource()));
                        }
                    } else {
                        resource = setProfile(resource);
                    }


                    VersionConvertor_30_40 convertor = new VersionConvertor_30_40();
                    IBaseResource convertedResource = convertor.convertResource((org.hl7.fhir.dstu3.model.Resource) resource, true);
                    try {
                        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(convertedResource));
                        results = this.fhirValidator.validateWithResult(convertedResource);
                    } catch (Exception val) {
                        log.error(val.getMessage());
                        return true;
                    }

                    //OperationOutcome outcomeR4 = ;

                    OperationOutcome outcome = OperationOutcomeFactory.removeUnsupportedIssues((org.hl7.fhir.r4.model.OperationOutcome) results.toOperationOutcome(), null);

                    if (!pass(outcome)) {
                        log.info("VALIDATION FAILED");
                        System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(outcome));
                        throw new UnprocessableEntityException(theRequestDetails.getServer().getFhirContext(), outcome);
                    }
                }
                return true;
            }
        }
    }

    public Boolean pass(OperationOutcome outcome) {

        for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {
            switch (issue.getSeverity()) {
                case ERROR:
                case FATAL:
                case WARNING:
                    return false;
            }
        }
        return true;
    }


    public IBaseResource setProfile(IBaseResource resource) {
        if (resource.getMeta() != null) {
            if (resource.getMeta().getProfile().size()>0) {
                for (int i =0; i<resource.getMeta().getProfile().size(); i++)
                    if (resource.getMeta().getProfile().get(i).toString().contains("CareConnect")) {
                        return resource;
                    }
            }
        }
        log.info("Adding profile to "+resource.getClass().getSimpleName());
        switch(resource.getClass().getSimpleName()) {
            case "Patient":
                ((Patient) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Patient_1));
                break;
            case "Practitioner":
                ((Practitioner) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Practitioner_1));
                break;
            case "PractitionerRole":
                ((PractitionerRole) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.PractitionerRole_1));
                break;
            case "Organization":
                log.info("Org called");
                ((Organization) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Organization_1));
                break;
            case "Location":
                ((Location) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Location_1));
                break;
            case "Observation":
                // Observation is not currently profiled on STU3 Care Connect
                ((Observation) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Observation_1));
                break;
            case "Encounter":
                ((Encounter) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Encounter_1));
                break;
            case "Condition":
                ((Condition) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Condition_1));
                break;
            case "Procedure":
                ((Procedure) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Procedure_1));
                break;
            case "Immunization":
                ((Immunization) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Immunization_1));
                break;
            case "MedicationRequest":
                ((MedicationRequest) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.MedicationRequest_1));
                break;
            case "MedicationStatement":
                ((MedicationStatement) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.MedicationStatement_1));
                break;
            case "AllergyIntolerance":
                ((AllergyIntolerance) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.AllergyIntolerance_1));
                break;
            case "Medication":
                ((Medication) resource).getMeta().getProfile().add(new UriType(CareConnectProfile.Medication_1));
                break;
            case "Flag":
                ((Flag) resource).getMeta().getProfile().add(new UriType("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Flag-1"));
                break;
            case "DocumentReference":
                ((DocumentReference) resource).getMeta().getProfile().add(new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-DocumentReference-1"));
                break;


        }
        return resource;
    }

}
