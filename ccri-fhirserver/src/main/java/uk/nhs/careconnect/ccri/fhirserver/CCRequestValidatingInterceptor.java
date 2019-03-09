package uk.nhs.careconnect.ccri.fhirserver;

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
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import uk.nhs.careconnect.ccri.fhirserver.provider.ResourceTestProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

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
                if (theRequest.getMethod().equals("POST") || theRequest.getMethod().equals("PUT")) {
                    results = this.fhirValidator.validateWithResult(requestText);
                    if (!results.isSuccessful()) {
                        log.info("VALIDATION FAILED");
                        System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(this.results.toOperationOutcome()));
                        throw new UnprocessableEntityException(theRequestDetails.getServer().getFhirContext(), this.results.toOperationOutcome());
                    }
                }
                return true;
            }
        }
    }



}
