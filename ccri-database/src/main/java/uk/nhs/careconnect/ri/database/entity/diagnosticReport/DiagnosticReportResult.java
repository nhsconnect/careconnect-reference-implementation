package uk.nhs.careconnect.ri.database.entity.diagnosticReport;

import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;

import javax.persistence.*;


@Entity
@Table(name="DiagnosticReportResult", uniqueConstraints= @UniqueConstraint(name="PK_DIAGNOSTIC_REPORT_RESULT", columnNames={"DIAGNOSTIC_REPORT_RESULT_ID"})
		)
public class  DiagnosticReportResult {

	public DiagnosticReportResult() {

	}

	public DiagnosticReportResult(DiagnosticReportEntity diagnosticReport) {
		this.diagnosticReport = diagnosticReport;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "DIAGNOSTIC_REPORT_RESULT_ID")
	private Long resultId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "DIAGNOSTIC_REPORT_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_DIAGNOSTIC_REPORT_RESULT"))
	private DiagnosticReportEntity diagnosticReport;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_OBSERVATION"))

	private ObservationEntity observation;

	public DiagnosticReportEntity getDiagnosticReport() {
	        return this.diagnosticReport;
	}

	public void setDiagnosticReport(DiagnosticReportEntity diagnosticReport) {
	        this.diagnosticReport = diagnosticReport;
	}

	public Long getResultId() {
		return resultId;
	}

	public void setResultId(Long resultId) {
		this.resultId = resultId;
	}

	public ObservationEntity getObservation() {
		return observation;
	}

	public void setObservation(ObservationEntity observation) {
		this.observation = observation;
	}
}
