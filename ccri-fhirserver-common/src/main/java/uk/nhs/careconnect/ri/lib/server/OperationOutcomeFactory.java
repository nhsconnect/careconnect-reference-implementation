package uk.nhs.careconnect.ri.lib.server;


import ca.uhn.fhir.rest.server.exceptions.*;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;

public class OperationOutcomeFactory {

    private OperationOutcomeFactory() { }

    public static BaseServerResponseException buildOperationOutcomeException(BaseServerResponseException exception, OperationOutcome.IssueSeverity code, OperationOutcome.IssueType issueType) {
        CodeableConcept codeableConcept = new CodeableConcept()
                .setText(exception.getMessage());


        OperationOutcome operationOutcome = new OperationOutcome();

        operationOutcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(issueType)
                .setDetails(codeableConcept);

       // operationOutcome.getMeta()
       //         .addProfile(SystemURL.SD_GPC_OPERATIONOUTCOME);

        exception.setOperationOutcome(operationOutcome);
        return exception;
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

    public static void convertToException (OperationOutcome outcome ) throws BaseServerResponseException {
        for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {

            // TODO Revist the mapping here.
            String text = null;
            String diagnostics = "";
            if (issue.getDiagnostics() != null ) diagnostics = issue.getDiagnostics();
            if (issue.getDetails() != null) text =issue.getDetails().getText();

            if (text ==null && issue.getDiagnostics() !=null) text = issue.getDiagnostics();

            if (text==null) text = "Unknown Reason";

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


                    /*
                    registerExceptionType(MethodNotAllowedException.STATUS_CODE, MethodNotAllowedException.class);
                    registerExceptionType(NotImplementedOperationException.STATUS_CODE, NotImplementedOperationException.class);
                    registerExceptionType(NotModifiedException.STATUS_CODE, NotModifiedException.class);

                    registerExceptionType(ResourceGoneException.STATUS_CODE, ResourceGoneException.class);
                    registerExceptionType(PreconditionFailedException.STATUS_CODE, PreconditionFailedException.class);

                   */
                default:
                    throw new UnprocessableEntityException(text, outcome);

            }
        }
        // Catch all
        throw new UnprocessableEntityException("Unknown Error", outcome);
    }
}
