package uk.nhs.careconnect.ri.database.entity.clinicialImpression;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentEntity;

import javax.persistence.*;

@Entity
@Table(name="ClinicalImpressionBasedOn", uniqueConstraints= @UniqueConstraint(name="PK_IMPRESSION_BASEDON", columnNames={"IMPRESSION_BASEDON_ID"})
		,indexes = {}
		)
public class ClinicalImpressionPrognosis extends BaseResource {

	public ClinicalImpressionPrognosis() {
	}
    public ClinicalImpressionPrognosis(ClinicalImpressionEntity impression) {
		this.impression = impression;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "IMPRESSION_BASEDON_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "IMPRESSION_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_BASEDON_IMPRESSION_ID"))
	private ClinicalImpressionEntity impression;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PROGNOSIS_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_IMPRESSION_PROGNOSIS_CODE_ID"))
    private ConceptEntity prognosisCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PROGNOSIS_RISK_ID",nullable = true,foreignKey= @ForeignKey(name="FK_IMPRESSION_PROGNOSIS_RISK_ID"))
    private RiskAssessmentEntity prognosisRisk;

	public Long getPredictionId() { return identifierId; }
	public void setPredictionId(Long identifierId) { this.identifierId = identifierId; }

	public ClinicalImpressionEntity getClinicalImpression() {
		return impression;
	}

	public void setClinicalImpression(ClinicalImpressionEntity impression) {
		this.impression = impression;
	}

    public Long getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(Long identifierId) {
        this.identifierId = identifierId;
    }

    public ClinicalImpressionEntity getImpression() {
        return impression;
    }

    public void setImpression(ClinicalImpressionEntity impression) {
        this.impression = impression;
    }

    public ConceptEntity getPrognosisCode() {
        return prognosisCode;
    }

    public void setPrognosisCode(ConceptEntity prognosisCode) {
        this.prognosisCode = prognosisCode;
    }

    public RiskAssessmentEntity getPrognosisRisk() {
        return prognosisRisk;
    }

    public void setPrognosisRisk(RiskAssessmentEntity prognosisRisk) {
        this.prognosisRisk = prognosisRisk;
    }

    @Override
	public Long getId() {
		return null;
	}
}
