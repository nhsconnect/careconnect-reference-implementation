package uk.nhs.careconnect.ri.database.entity.claim;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;

import javax.persistence.*;

@Entity
@Table(name="ClaimDiagnosis", uniqueConstraints= @UniqueConstraint(name="PK_CLAIM_DIAGNOSIS", columnNames={"CLAIM_DIAGNOSIS_ID"})
		,indexes = {}
		)
public class ClaimDiagnosis extends BaseResource {

	public ClaimDiagnosis() {
	}
    public ClaimDiagnosis(ClaimEntity claim) {
		this.claim = claim;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CLAIM_DIAGNOSIS_ID")
    private Long diagnosis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CLAIM_ID",foreignKey= @ForeignKey(name="FK_CLAIM_DIAGNOSIS_CLAIM_ID"))
    private ClaimEntity claim;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CLAIM_CONDITION_ID", nullable = false, foreignKey= @ForeignKey(name="FK_CAREPLAN_CONDITION_ID"))
	private ConditionEntity condition;

	public ClaimEntity getClaim() {
		return claim;
	}

	public void setClaim(ClaimEntity claim) {
		this.claim = claim;
	}

	@Override
	public Long getId() {
		return diagnosis;
	}

	public Long getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(Long diagnosis) {
		this.diagnosis = diagnosis;
	}

	public ConditionEntity getCondition() {
		return condition;
	}

	public void setCondition(ConditionEntity condition) {
		this.condition = condition;
	}
}
