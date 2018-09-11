package uk.nhs.careconnect.ri.database.entity.clinicialImpression;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;

import javax.persistence.*;

@Entity
@Table(name="ClinicalImpressionBasis", uniqueConstraints= @UniqueConstraint(name="PK_IMPRESSION_BASIS", columnNames={"IMPRESSION_BASIS_ID"})
		,indexes = {}
		)
public class ClinicalImpressionFinding extends BaseResource {

	public ClinicalImpressionFinding() {
	}
    public ClinicalImpressionFinding(ClinicalImpressionEntity impression) {
		this.impression = impression;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "IMPRESSION_BASIS_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "IMPRESSION_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_BASIS_IMPRESSION_ID"))
	private ClinicalImpressionEntity impression;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ITEM_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_IMPRESSION_ITEM_CONCEPT_ID"))
	private ConceptEntity itemCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ITEM_CONDITION_ID",nullable = true,foreignKey= @ForeignKey(name="FK_IMPRESSION_ITEM_CONDITION_ID"))
	private ConditionEntity itemCondition;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ITEM_OBSERVATIAON_ID",nullable = true,foreignKey= @ForeignKey(name="FK_IMPRESSION_ITEM_OBSERVATION_ID"))
	private ObservationEntity itemObservation;

	@Column(name="BASIS")
	private String basis;

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

    public ConceptEntity getItemCode() {
        return itemCode;
    }

    public void setItemCode(ConceptEntity itemCode) {
        this.itemCode = itemCode;
    }

    public ConditionEntity getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(ConditionEntity itemCondition) {
        this.itemCondition = itemCondition;
    }

    public ObservationEntity getItemObservation() {
        return itemObservation;
    }

    public void setItemObservation(ObservationEntity itemObservation) {
        this.itemObservation = itemObservation;
    }

    public String getBasis() {
        return basis;
    }

    public void setBasis(String basis) {
        this.basis = basis;
    }

    @Override
	public Long getId() {
		return null;
	}
}
