package uk.nhs.careconnect.ri.database.entity.diagnosticReport;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;


import javax.persistence.*;


@Entity
@Table(name="DiagnosticReportDiagnosis", uniqueConstraints= @UniqueConstraint(name="PK_DIAGNOSTIC_REPORT_DIAGNOSIS", columnNames={"DIAGNOSTIC_REPORT_DIAGNOSIS_ID"})
		)
public class DiagnosticReportDiagnosis {

	public DiagnosticReportDiagnosis() {

	}

	public DiagnosticReportDiagnosis(DiagnosticReportEntity diagnosticReport) {
		this.diagnosticReport = diagnosticReport;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "DIAGNOSTIC_REPORT_DIAGNOSIS_ID")
	private Long diagnosisId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "DIAGNOSTIC_REPORT_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_DIAGNOSTIC_REPORT_DIAGNOSIS"))
	private DiagnosticReportEntity diagnosticReport;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_CONCEPT"))

	private ConceptEntity diagnosis;

    public Long getDiagnosisId() { return diagnosisId; }
	public void setDiagnosisId(Long diagnosisId) { this.diagnosisId = diagnosisId; }

	public DiagnosticReportEntity getDiagnosticReport() {
	        return this.diagnosticReport;
	}

	public void setDiagnosticReport(DiagnosticReportEntity diagnosticReport) {
	        this.diagnosticReport = diagnosticReport;
	}

	public ConceptEntity getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(ConceptEntity diagnosis) {
		this.diagnosis = diagnosis;
	}
}
