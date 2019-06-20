package uk.nhs.careconnect.ri.database.entity.claim;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimEntity;

import javax.persistence.*;

@Entity
@Table(name="ClaimIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_CLAIM_IDENTIFIER", columnNames={"CLAIM_IDENTIFIER_ID"})
		,indexes = {}
		)
public class ClaimIdentifier extends BaseIdentifier {

	public ClaimIdentifier() {
	}
    public ClaimIdentifier(ClaimEntity claim) {
		this.claim = claim;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CLAIM_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CLAIM_ID",foreignKey= @ForeignKey(name="FK_CLAIM_IDENTIFIER_CLAIM_ID"))

    private ClaimEntity claim;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ClaimEntity getClaim() {
		return claim;
	}

	public void setClaim(ClaimEntity claim) {
		this.claim = claim;
	}


}
