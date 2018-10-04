package uk.nhs.careconnect.ri.lib.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.fhir.OperationOutcomeException;

import java.io.*;

public class ProviderResponseLibrary {
    private static final Logger log = LoggerFactory.getLogger(ProviderResponseLibrary.class);

    public static MethodOutcome handleException(MethodOutcome method, Exception ex) {
        if (ex instanceof OperationOutcomeException) {

            OperationOutcomeException outcomeException = (OperationOutcomeException) ex;
            if (outcomeException.getOutcome().hasIssue()) {
                log.error(outcomeException.getOutcome().getIssueFirstRep().getDiagnostics());
            }
            method.setOperationOutcome(outcomeException.getOutcome());
            method.setCreated(false);
        } else {
            log.error(ex.getMessage());
            if (ex.getStackTrace().length >0) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                log.error(exceptionAsString);

            }
            if (ex.getCause() != null) {
                log.error(ex.getCause().toString());
            }
            method.setCreated(false);
            method.setOperationOutcome(OperationOutcomeFactory.createOperationOutcome(ex.getMessage()));
        }

        return method;
    }

    public static void createException(FhirContext ctx, IBaseResource resource) throws Exception {
        if (resource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) resource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));

            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Server Error",(OperationOutcome) resource);
        }
    }

    public static IBaseResource processMessageBody(FhirContext ctx, IBaseResource resource, Object message ) {
        InputStream inputStream = null;

        if (message instanceof InputStream) {
            log.trace("RESPONSE InputStream");
            inputStream = (InputStream) message;
            Reader reader = new InputStreamReader(inputStream);
            try {
                resource = ctx.newXmlParser().parseResource(reader);
            } catch (Exception ex) {
                resource = ctx.newJsonParser().parseResource(reader);
            }
        } else
        if (message instanceof String) {
            log.trace("RESPONSE String = "+(String) message);
            try {
                resource = ctx.newXmlParser().parseResource((String) message);
            } catch (Exception ex) {
                resource = ctx.newJsonParser().parseResource((String) message);
            }
            log.trace("RETURNED String Resource "+resource.getClass().getSimpleName());
        } else {
            log.info("MESSAGE TYPE "+message.getClass());
        }
        return resource;
    }

    public static MethodOutcome setMethodOutcome (IBaseResource resource, MethodOutcome method) {
        if (resource instanceof OperationOutcome) {
            OperationOutcome opOutcome = (OperationOutcome) resource;
            method.setOperationOutcome(opOutcome);
            method.setCreated(false);
        } else {
            method.setCreated(true);
            OperationOutcome opOutcome = new OperationOutcome();
            method.setOperationOutcome(opOutcome);
            method.setId(resource.getIdElement());
            method.setResource(resource);
        }
        return method;
    }
}
