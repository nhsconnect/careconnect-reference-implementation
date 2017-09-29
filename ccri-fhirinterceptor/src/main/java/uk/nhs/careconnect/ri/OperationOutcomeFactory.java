package uk.nhs.careconnect.ri;


import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
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
}
