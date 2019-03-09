package uk.nhs.careconnect.ccri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.rest.server.method.ResourceParameter;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import uk.nhs.careconnect.ccri.fhirserver.provider.ResourceTestProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CCRequestValidatingInterceptor extends InterceptorAdapter {
    private Logger log = null; //LoggerFactory.getLogger(ServerInterceptor.class);
    private String myErrorMessageFormat = "ERROR - ${operationType} - ${idOrResourceName}";

    private FhirValidator fhirValidator;

    ValidationResult results;

    FhirContext ctx = FhirContext.forDstu3();

    public CCRequestValidatingInterceptor(Logger ourLog, FhirValidator fhirValidator) {
        super();
        this.log = ourLog;
        this.fhirValidator = fhirValidator;
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
                    results = this.fhirValidator.validateWithResult(requestText);

                    OperationOutcome outcome = (OperationOutcome) results.toOperationOutcome();

                    List<OperationOutcome.OperationOutcomeIssueComponent> issueRemove = new ArrayList<>();
                    for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {
                        Boolean remove = false;

                        if (issue.getDiagnostics().contains("ValueSet http://snomed.info/sct not found")) {
                            remove = true;
                        }
                        if (issue.getDiagnostics().contains("Could not verify slice for profile https://fhir.nhs.uk/STU3/StructureDefinition")) {
                            remove = true;
                        }
                        if (issue.getDiagnostics().contains("http://snomed.info/sct")) {
                            remove = true;
                        }
                        if (issue.getDiagnostics().contains("(fhirPath = true and (use memberOf 'https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NameUse-1'))")) {
                            remove = true;
                        }
                        if (remove) {
                            log.info("Stripped "+issue.getDiagnostics());
                            issueRemove.add(issue);
                        }
                    }
                    outcome.getIssue().removeAll(issueRemove);
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
                    return false;
            }
        }
        return true;
    }


}
