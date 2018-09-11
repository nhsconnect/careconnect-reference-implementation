package uk.nhs.careconnect.ri.database.entity.riskAssessment;


import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;

import javax.persistence.*;

@Entity
@Table(name="RiskAssessmentBasedOn", uniqueConstraints= @UniqueConstraint(name="PK_RISK_BASEDON", columnNames={"RISK_BASEDON_ID"})
		,indexes = {}
		)
public class RiskAssessmentBasedOn extends BaseReferenceItem {

	public RiskAssessmentBasedOn() {
	}
    public RiskAssessmentBasedOn(RiskAssessmentEntity risk) {
		this.risk = risk;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "RISK_BASEDON_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "RISK_ID",foreignKey= @ForeignKey(name="FK_RISK_BASEDON_RISK_ID"))
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
