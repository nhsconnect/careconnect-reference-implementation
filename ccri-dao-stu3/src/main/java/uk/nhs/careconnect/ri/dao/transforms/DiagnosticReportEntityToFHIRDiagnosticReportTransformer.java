package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportEntity;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportIdentifier;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportResult;


@Component
public class DiagnosticReportEntityToFHIRDiagnosticReportTransformer implements Transformer<DiagnosticReportEntity, DiagnosticReport> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DiagnosticReportEntityToFHIRDiagnosticReportTransformer.class);


    @Override
    public DiagnosticReport transform(final DiagnosticReportEntity diagnosticReportEntity) {
        final DiagnosticReport diagnosticReport = new DiagnosticReport();

        Meta meta = new Meta();
                //.addProfile(CareConnectProfile.Condition_1);

        if (diagnosticReportEntity.getUpdated() != null) {
            meta.setLastUpdated(diagnosticReportEntity.getUpdated());
        }
        else {
            if (diagnosticReportEntity.getCreated() != null) {
                meta.setLastUpdated(diagnosticReportEntity.getCreated());
            }
        }
        diagnosticReport.setMeta(meta);

        diagnosticReport.setId(diagnosticReportEntity.getId().toString());



        if (diagnosticReportEntity.getStatus() != null) {
            diagnosticReport.setStatus(diagnosticReportEntity.getStatus());
        }

        if (diagnosticReportEntity.getPatient() != null) {
            diagnosticReport
                    .setSubject(new Reference("Patient/"+diagnosticReportEntity.getPatient().getId())
                    .setDisplay(diagnosticReportEntity.getPatient().getNames().get(0).getDisplayName()));
        }



        for (DiagnosticReportIdentifier identifier : diagnosticReportEntity.getIdentifiers()) {
            diagnosticReport.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        for (DiagnosticReportResult reportResult : diagnosticReportEntity.getResults()) {
            diagnosticReport.addResult(new Reference("Observation/"+reportResult.getObservation().getId()));
        }

        return diagnosticReport;

    }
}
