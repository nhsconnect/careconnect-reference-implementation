package uk.nhs.careconnect.ri.database.entity.riskAssessment;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import javax.persistence.*;

@Entity
@Table(name="RiskAssessmentIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_RISK_IDENTIFIER", columnNames={"RISK_IDENTIFIER_ID"})
		,indexes = {}
		)
public class RiskAssessmentIdentifier extends BaseIdentifier {

	public RiskAssessmentIdentifier() {
	}
    public RiskAssessmentIdentifier(RiskAssessmentEntity risk) {
		this.risk = risk;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "RISK_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "RISK_ID",foreignKey= @ForeignKey(name="FK_RISK_IDENTIFIER_RISK_ID"))

    private RiskAssessmentEntity risk;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public RiskAssessmentEntity getRiskAssessment() {
		return risk;
	}

	public void setRiskAssessment(RiskAssessmentEntity risk) {
		this.risk = risk;
	}


}
