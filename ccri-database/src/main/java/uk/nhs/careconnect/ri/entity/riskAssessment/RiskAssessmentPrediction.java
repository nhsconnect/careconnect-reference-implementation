package uk.nhs.careconnect.ri.entity.riskAssessment;


import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="RiskAssessmentPrediction", uniqueConstraints= @UniqueConstraint(name="PK_RISK_PREDICTION", columnNames={"RISK_PREDICTION_ID"})
		,indexes = {}
		)
public class RiskAssessmentPrediction extends BaseResource {

	public RiskAssessmentPrediction() {
	}
    public RiskAssessmentPrediction(RiskAssessmentEntity risk) {
		this.risk = risk;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "RISK_PREDICTION_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "RISK_ID",foreignKey= @ForeignKey(name="FK_RISK_PREDICTION_RISK_ID"))
	private RiskAssessmentEntity risk;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="OUTCOMCE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_RISK_OUTCOME_CONCEPT_ID"))
	private ConceptEntity outcome;


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
