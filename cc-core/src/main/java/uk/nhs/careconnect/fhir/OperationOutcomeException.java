package uk.nhs.careconnect.fhir;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;

public class OperationOutcomeException extends Exception {
    private OperationOutcome outcome;

    public OperationOutcomeException(OperationOutcome operationOutcome) {
        super();
        this.outcome = operationOutcome;
    }

    public OperationOutcomeException(String message, String diagnostics, OperationOutcome.IssueType type) {
        super();
        this.outcome = new OperationOutcome();

        this.outcome.addIssue()
                .setCode(type)
                .setSeverity(OperationOutcome.IssueSeverity.FATAL)
                .setDiagnostics(diagnostics)
                .setDetails(
                        new CodeableConcept().setText(message)
                );

    }

    public OperationOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(OperationOutcome outcome) {
        this.outcome = outcome;
    }
}
