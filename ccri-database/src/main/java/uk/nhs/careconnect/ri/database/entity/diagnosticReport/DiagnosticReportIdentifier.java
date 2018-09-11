package uk.nhs.careconnect.ri.database.entity.diagnosticReport;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="DiagnosticReportIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_DIAGNOSTIC_REPORT_IDENTIFIER", columnNames={"DIAGNOSTIC_REPORT_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_DIAGNOSTIC_REPORT_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class DiagnosticReportIdentifier extends BaseIdentifier {

	public DiagnosticReportIdentifier() {

	}

	public DiagnosticReportIdentifier(DiagnosticReportEntity diagnosticReport) {
		this.diagnosticReport = diagnosticReport;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "DIAGNOSTIC_REPORT_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "DIAGNOSTIC_REPORT_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_DIAGNOSTIC_REPORT_IDENTIFIER"))
	private DiagnosticReportEntity diagnosticReport;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public DiagnosticReportEntity getDiagnosticReport() {
	        return this.diagnosticReport;
	}

	public void setDiagnosticReport(DiagnosticReportEntity diagnosticReport) {
	        this.diagnosticReport = diagnosticReport;
	}




}
