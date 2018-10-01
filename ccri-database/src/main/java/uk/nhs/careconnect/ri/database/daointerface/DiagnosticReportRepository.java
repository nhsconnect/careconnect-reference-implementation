package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportEntity;

import java.util.List;

public interface DiagnosticReportRepository extends BaseRepository<DiagnosticReportEntity,DiagnosticReport> {
    void save(FhirContext ctx, DiagnosticReportEntity diagnosticReport) throws OperationOutcomeException;

    DiagnosticReport read(FhirContext ctx, IdType theId);

    DiagnosticReportEntity readEntity(FhirContext ctx, IdType theId);

    DiagnosticReport create(FhirContext ctx, DiagnosticReport diagnosticReport, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<DiagnosticReport> search(FhirContext ctx,

                             @OptionalParam(name = DiagnosticReport.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = DiagnosticReport.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = DiagnosticReport.SP_RES_ID) StringParam id

    );

    List<DiagnosticReportEntity> searchEntity(FhirContext ctx,
                                         @OptionalParam(name = DiagnosticReport.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = DiagnosticReport.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = DiagnosticReport.SP_RES_ID) StringParam id
    );
}
