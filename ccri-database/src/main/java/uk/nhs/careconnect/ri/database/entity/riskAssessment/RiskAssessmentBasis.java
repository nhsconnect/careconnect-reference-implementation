package uk.nhs.careconnect.ri.database.entity.riskAssessment;


import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;

import javax.persistence.*;

@Entity
@Table(name="RiskAssessmentBasis", uniqueConstraints= @UniqueConstraint(name="PK_RISK_BASIS", columnNames={"RISK_BASIS_ID"})
		,indexes = {}
		)
public class RiskAssessmentBasis extends BaseReferenceItem {

	public RiskAssessmentBasis() {
	}
    public RiskAssessmentBasis(RiskAssessmentEntity risk) {
		this.risk = risk;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "RISK_BASIS_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "RISK_ID",foreignKey= @ForeignKey(name="FK_RISK_BASIS_RISK_ID"))
	private RiskAssessmentEntity risk;



	public Long getPredictionId() { return identifierId; }
	public void setPredictionId(Long identifierId) { this.identifierId = identifierId; }

	public RiskAssessmentEntity getRiskAssessment() {
		return risk;
	}

	public void setRiskAssessment(RiskAssessmentEntity risk) {
		this.risk = risk;
	}


	@Override
	public Long getId() {
		return null;
	}
}
