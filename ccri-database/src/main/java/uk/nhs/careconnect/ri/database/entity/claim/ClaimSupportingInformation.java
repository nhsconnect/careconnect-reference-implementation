package uk.nhs.careconnect.ri.database.entity.claim;

import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;


import javax.persistence.*;

@Entity
@Table(name="ClaimSupportingInformation", uniqueConstraints= @UniqueConstraint(name="PK_CLAIM_SUPP_INFO", columnNames={"CLAIM_SUPP_INFO_ID"})
		,indexes = {}
		)
public class ClaimSupportingInformation extends BaseReferenceItem {

	public ClaimSupportingInformation() {
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CLAIM_SUPP_INFO_ID")
    private Long infoId;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CLAIM_ID",foreignKey= @ForeignKey(name="FK_CLAIM_SUPP_INFO_CLAIM_ID"))
	private ClaimEntity claim;

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public Long getInfoId() {
		return infoId;
	}

	public void setInfoId(Long infoId) {
		this.infoId = infoId;
	}

	public ClaimEntity getClaim() {
		return claim;
	}

	public void setClaim(ClaimEntity claim) {
		this.claim = claim;
	}



}
