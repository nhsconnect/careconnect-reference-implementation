package uk.nhs.careconnect.ri.database.entity.riskAssessment;


import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

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

	@Column(name = "PROBABILITY")
	private BigDecimal probablity;

	@Column(name = "PROBABILITY_LOW")
	private BigDecimal probablityRangeLow;

	@Column(name = "PROBABILITY_HIGH")
	private BigDecimal probablityRangeHigh;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="QUALITIVE_RISK_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_RISK_QUALITIVE_RISK_CONCEPT_ID"))
	private ConceptEntity qualitiveRiskConcept;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "WHEN_START_DATETIME")
	private Date whenStartDateTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "WHEN_END_DATETIME")
	private Date whenEndDateTime;

	@Column(name = "WHEN_LOW")
	private BigDecimal whenLow;

	@Column(name = "WHEN_HIGH")
	private BigDecimal whenHigh;

	@Column(name="RATIONALE")
	private String rationale;

	public Long getPredictionId() { return identifierId; }
	public void setPredictionId(Long identifierId) { this.identifierId = identifierId; }

	public RiskAssessmentEntity getRiskAssessment() {
		return risk;
	}

	public void setRiskAssessment(RiskAssessmentEntity risk) {
		this.risk = risk;
	}

    public Long getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(Long identifierId) {
        this.identifierId = identifierId;
    }

    public RiskAssessmentEntity getRisk() {
        return risk;
    }

    public void setRisk(RiskAssessmentEntity risk) {
        this.risk = risk;
    }

    public ConceptEntity getOutcome() {
        return outcome;
    }

    public void setOutcome(ConceptEntity outcome) {
        this.outcome = outcome;
    }

    public BigDecimal getProbablity() {
        return probablity;
    }

    public void setProbablity(BigDecimal probablity) {
        this.probablity = probablity;
    }

    public BigDecimal getProbablityRangeLow() {
        return probablityRangeLow;
    }

    public void setProbablityRangeLow(BigDecimal probablityRangeLow) {
        this.probablityRangeLow = probablityRangeLow;
    }

    public BigDecimal getProbablityRangeHigh() {
        return probablityRangeHigh;
    }

    public void setProbablityRangeHigh(BigDecimal probablityRangeHigh) {
        this.probablityRangeHigh = probablityRangeHigh;
    }

    public ConceptEntity getQualitiveRiskConcept() {
        return qualitiveRiskConcept;
    }

    public void setQualitiveRiskConcept(ConceptEntity qualitiveRiskConcept) {
        this.qualitiveRiskConcept = qualitiveRiskConcept;
    }

    public Date getWhenStartDateTime() {
        return whenStartDateTime;
    }

    public void setWhenStartDateTime(Date whenStartDateTime) {
        this.whenStartDateTime = whenStartDateTime;
    }

    public Date getWhenEndDateTime() {
        return whenEndDateTime;
    }

    public void setWhenEndDateTime(Date whenEndDateTime) {
        this.whenEndDateTime = whenEndDateTime;
    }

    public BigDecimal getWhenLow() {
        return whenLow;
    }

    public void setWhenLow(BigDecimal whenLow) {
        this.whenLow = whenLow;
    }

    public BigDecimal getWhenHigh() {
        return whenHigh;
    }

    public void setWhenHigh(BigDecimal whenHigh) {
        this.whenHigh = whenHigh;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    @Override
	public Long getId() {
		return null;
	}
}
