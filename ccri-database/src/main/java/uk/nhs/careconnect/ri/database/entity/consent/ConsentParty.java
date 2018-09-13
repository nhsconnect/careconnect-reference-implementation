package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;

import javax.persistence.*;

@Entity
@Table(name="ConsentParty", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_PARTY", columnNames={"CONSENT_PARTY_ID"})
		,indexes = {}
		)
public class ConsentParty extends BaseReferenceItem {

	public ConsentParty() {
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONSENT_PARTY_ID")
    private Long infoId;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_PARTY_CONSENT_ID"))
	private ConsentEntity consent;

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

	public ConsentEntity getConsent() {
		return consent;
	}

	public void setConsent(ConsentEntity consent) {
		this.consent = consent;
	}



}
