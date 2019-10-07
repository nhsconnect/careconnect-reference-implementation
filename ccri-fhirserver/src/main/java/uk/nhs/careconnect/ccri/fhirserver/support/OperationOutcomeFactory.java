package uk.nhs.careconnect.ccri.fhirserver.support;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.*;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OperationOutcomeFactory {

    private OperationOutcomeFactory() { }

    private static final Logger log = LoggerFactory.getLogger(OperationOutcomeFactory.class);

    public static BaseServerResponseException buildOperationOutcomeException(BaseServerResponseException exception, OperationOutcome.IssueType issueType) {
        CodeableConcept codeableConcept = new CodeableConcept()
                .setText(exception.getMessage());


        OperationOutcome operationOutcome = new OperationOutcome();

        operationOutcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(issueType)
                .setDetails(codeableConcept);


        exception.setOperationOutcome(operationOutcome);
        return exception;
    }

    public static BaseServerResponseException buildOperationOutcomeException(BaseServerResponseException exception,  org.hl7.fhir.r4.model.OperationOutcome.IssueType issueType) {
        org.hl7.fhir.r4.model.CodeableConcept codeableConcept = new org.hl7.fhir.r4.model.CodeableConcept()
                .setText(exception.getMessage());


        org.hl7.fhir.r4.model.OperationOutcome operationOutcome = new org.hl7.fhir.r4.model.OperationOutcome();

        operationOutcome.addIssue()
                .setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR)
                .setCode(issueType)
                .setDetails(codeableConcept);

         exception.setOperationOutcome(operationOutcome);
        return exception;
    }

    public static OperationOutcome removeUnsupportedIssues(org.hl7.fhir.r4.model.OperationOutcome outcome, FhirContext ctx) {

        // This function converts from R4 to STU3 and then calls the STU3 version

        if (ctx != null) {
            log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(outcome));
        }
        OperationOutcome operationOutcome = new OperationOutcome();
        // TODO - Basic Conversion from r4 to stu3
        if (outcome.hasIssue()) {
            for (org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {
                OperationOutcome.OperationOutcomeIssueComponent r3issue = operationOutcome.addIssue();
                if (issue.hasCode()) {
                    if (issue.getCode().equals(org.hl7.fhir.r4.model.OperationOutcome.IssueType.PROCESSING)) {
                        r3issue.setCode(OperationOutcome.IssueType.PROCESSING);
                    } else {
                                    log.error("Missing {}", issue.getCode().getDisplay());
                    }
                }
                if (issue.hasSeverity()) {
                    switch (issue.getSeverity()) {
                        case ERROR:
                            r3issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
                            break;
                        case INFORMATION:
                            r3issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
                            break;
                        case FATAL:
                            r3issue.setSeverity(OperationOutcome.IssueSeverity.FATAL);
                            break;
                        case WARNING:
                            r3issue.setSeverity(OperationOutcome.IssueSeverity.WARNING);
                            break;
                        case NULL:
                            r3issue.setSeverity(OperationOutcome.IssueSeverity.NULL);
                            break;
                    }
                }
                if (issue.hasDiagnostics()) {
                    r3issue.setDiagnostics(issue.getDiagnostics());
                }
                if (issue.hasLocation()) {
                    for (StringType stringType : issue.getLocation()) {
                        r3issue.addLocation(stringType.getValue());
                    }
                }
            }
        }

        return removeUnsupportedIssues(operationOutcome);
    }



    public static OperationOutcome removeUnsupportedIssues(OperationOutcome outcome) {



        List<OperationOutcome.OperationOutcomeIssueComponent> issueRemove = new ArrayList<>();
        for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {
            boolean remove = false;


            // Fault in profile?? Yes

            if (issue.getDiagnostics().contains("(fhirPath = true and (use memberOf 'https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NameUse-1'))")) {
                remove = true;
            }

            if (issue.getDiagnostics().contains("Patient.name[official]")) {
                remove = true;
            }

            // Need to check further, poss hapi issue?

            if (issue.getDiagnostics().contains("Could not verify slice for profile https://fhir.nhs.uk/STU3/StructureDefinition")) {
                remove = true;
            }

            // Appears to be a fault in CareConnect profiles

            if (issue.getDiagnostics().contains("Could not match discriminator (code) for slice Observation")) {
                remove = true;
            }

            //
            // Logged as issue https://github.com/jamesagnew/hapi-fhir/issues/1235
           /* Fixed on r4 validator but remove if using stu3 validation */
           if (issue.getDiagnostics().contains("Entry isn't reachable by traversing from first Bundle entry")) {
                remove = true;
            }
            if (remove) {
                log.info("Stripped {}", issue.getDiagnostics());
                issueRemove.add(issue);
            }
        }
        outcome.getIssue().removeAll(issueRemove);
        return outcome;
    }

    public static OperationOutcome createOperationOutcome (String message) {
        OperationOutcome outcome = new OperationOutcome();

        outcome.addIssue()
                .setCode(OperationOutcome.IssueType.EXCEPTION)
                .setSeverity(OperationOutcome.IssueSeverity.FATAL)
                .setDiagnostics(message)
                .setDetails(
                        new CodeableConcept().setText(message)
                );
        return outcome;
    }

    public static OperationOutcome.IssueType getIssueType(Exception ex) {
        OperationOutcome.IssueType issueType = OperationOutcome.IssueType.PROCESSING;
        if (ex instanceof ResourceNotFoundException) issueType=OperationOutcome.IssueType.NOTFOUND;
        if (ex instanceof AuthenticationException) issueType=OperationOutcome.IssueType.SECURITY;
        if (ex instanceof InvalidRequestException) issueType=OperationOutcome.IssueType.INVALID;
        if (ex instanceof InternalErrorException) issueType=OperationOutcome.IssueType.EXCEPTION;
        if (ex instanceof ForbiddenOperationException) issueType=OperationOutcome.IssueType.FORBIDDEN;
        if (ex instanceof ResourceVersionConflictException) issueType=OperationOutcome.IssueType. CONFLICT;
        if (ex instanceof NotImplementedOperationException) issueType=OperationOutcome.IssueType. NOTSUPPORTED;
        if (ex instanceof PreconditionFailedException) issueType=OperationOutcome.IssueType. DUPLICATE;
        if (ex instanceof MethodNotAllowedException) issueType=OperationOutcome.IssueType.BUSINESSRULE;
        return issueType;
    }

    private static void processCode(OperationOutcome outcome, OperationOutcome.OperationOutcomeIssueComponent issue, String text, String diagnostics) {
        switch (issue.getCode()) {
            case NOTFOUND:
                throw new ResourceNotFoundException(text,outcome);
            case PROCESSING:

                if (diagnostics.contains("The FHIR endpoint on this server does not know how to handle")) {
                    throw new NotImplementedOperationException(text,outcome);
                } else {
                    throw new UnprocessableEntityException(text, outcome);
                }
            case SECURITY:
                throw new AuthenticationException();
            case INVALID:
                // 400
                throw new InvalidRequestException(text,outcome);
            case EXCEPTION:
                throw new InternalErrorException(text,outcome);
            case FORBIDDEN:
                throw new ForbiddenOperationException(text,outcome);
            case CONFLICT:
                throw new ResourceVersionConflictException(text,outcome);
            case NOTSUPPORTED:
                // 501
                throw new NotImplementedOperationException(text,outcome);
            case DUPLICATE:
                throw new PreconditionFailedException(text,outcome);
            case BUSINESSRULE:
                /// Check this is 405
                throw new MethodNotAllowedException(text,outcome);

            default:
                throw new UnprocessableEntityException(text, outcome);

        }
    }
    public static void convertToException (OperationOutcome outcome ) {
        for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {


            String text = null;
            String diagnostics = "";
            if (issue.getDiagnostics() != null ) diagnostics = issue.getDiagnostics();
            if (issue.getDetails() != null) text =issue.getDetails().getText();

            if (text ==null && issue.getDiagnostics() !=null) text = issue.getDiagnostics();

            if (text==null) text = "Unknown Reason";

            processCode(outcome, issue, text, diagnostics);
        }
        // Catch all
        throw new UnprocessableEntityException("Unknown Error", outcome);
    }
}
