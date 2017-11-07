package uk.nhs.careconnect.ri.common;


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

    public static void convertToException (OperationOutcome outcome ) throws BaseServerResponseException {
        for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {

            // TODO Revist the mapping here.

            switch (issue.getCode()) {
                case NOTFOUND:
                    throw new ResourceNotFoundException(issue.getDetails().getText(),outcome);
                case PROCESSING:
                    throw new UnprocessableEntityException(issue.getDetails().getText(),outcome);
                case SECURITY:
                    throw new AuthenticationException();
                case INVALID:
                    // 400
                    throw new InvalidRequestException(issue.getDetails().getText(),outcome);
                case EXCEPTION:
                    throw new InternalErrorException(issue.getDetails().getText(),outcome);
                case FORBIDDEN:
                    throw new ForbiddenOperationException(issue.getDetails().getText(),outcome);
                case CONFLICT:
                    throw new ResourceVersionConflictException(issue.getDetails().getText(),outcome);
                case NOTSUPPORTED:
                    // 501
                    throw new NotImplementedOperationException(issue.getDetails().getText(),outcome);
                case DUPLICATE:
                    throw new PreconditionFailedException(issue.getDetails().getText(),outcome);


                    /*
                    registerExceptionType(MethodNotAllowedException.STATUS_CODE, MethodNotAllowedException.class);
                    registerExceptionType(NotImplementedOperationException.STATUS_CODE, NotImplementedOperationException.class);
                    registerExceptionType(NotModifiedException.STATUS_CODE, NotModifiedException.class);

                    registerExceptionType(ResourceGoneException.STATUS_CODE, ResourceGoneException.class);
                    registerExceptionType(PreconditionFailedException.STATUS_CODE, PreconditionFailedException.class);

                   */


            }
        }
    }
}
