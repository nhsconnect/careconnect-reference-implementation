package uk.nhs.careconnect.ri.database.entity.clinicialImpression;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="ClinicalImpressionIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_IMPRESSION_IDENTIFIER", columnNames={"IMPRESSION_IDENTIFIER_ID"})
		,indexes = {}
		)
public class ClinicalImpressionIdentifier extends BaseIdentifier {

	public ClinicalImpressionIdentifier() {
	}
    public ClinicalImpressionIdentifier(ClinicalImpressionEntity impression) {
		this.impression = impression;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "IMPRESSION_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "IMPRESSION_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_IDENTIFIER_IMPRESSION_ID"))

    private ClinicalImpressionEntity impression;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ClinicalImpressionEntity getClinicalImpression() {
		return impression;
	}

	public void setClinicalImpression(ClinicalImpressionEntity impression) {
		this.impression = impression;
	}


}
