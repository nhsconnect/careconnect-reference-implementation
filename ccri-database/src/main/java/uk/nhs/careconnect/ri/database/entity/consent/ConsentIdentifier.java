package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="ConsentIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_IDENTIFIER", columnNames={"CONSENT_IDENTIFIER_ID"})
		,indexes = {}
		)
public class ConsentIdentifier extends BaseIdentifier {

	public ConsentIdentifier() {
	}
    public ConsentIdentifier(ConsentEntity consent) {
		this.consent = consent;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONSENT_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_IDENTIFIER_CONSENT_ID"))

    private ConsentEntity consent;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ConsentEntity getConsent() {
		return consent;
	}

	public void setConsent(ConsentEntity consent) {
		this.consent = consent;
	}


}
