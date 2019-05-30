package uk.nhs.careconnect.ri.database.entity.claim;

import uk.nhs.careconnect.ri.database.entity.BaseCodeableConcept;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;

@Entity
@Table(name="ClaimRelated", uniqueConstraints= @UniqueConstraint(name="PK_CLAIM_RELATED", columnNames={"CLAIM_RELATED_ID"})
		,indexes = {}
		)
public class ClaimRelated extends BaseCodeableConcept {

	public ClaimRelated() {
	}
    public ClaimRelated(ClaimEntity claim) {
		this.claim = claim;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CLAIM_RELATED_ID")
    private Long relatedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CLAIM_ID",foreignKey= @ForeignKey(name="FK_CLAIM_RELATED_CLAIM_ID"))
	private ClaimEntity claim;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "RELATED_CLAIM_ID",foreignKey= @ForeignKey(name="FK_RELATED_CLAIM_ID"))
	private ClaimEntity relatedClaim;

	public ClaimEntity getClaim() {
		return claim;
	}

	public void setClaim(ClaimEntity claim) {
		this.claim = claim;
	}

	public Long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(Long relatedId) {
		this.relatedId = relatedId;
	}

	public ClaimEntity getRelatedClaim() {
		return relatedClaim;
	}

	public void setRelatedClaim(ClaimEntity relatedClaim) {
		this.relatedClaim = relatedClaim;
	}

	@Override
	public Long getId() {
		return getRelatedId();
	}
}
