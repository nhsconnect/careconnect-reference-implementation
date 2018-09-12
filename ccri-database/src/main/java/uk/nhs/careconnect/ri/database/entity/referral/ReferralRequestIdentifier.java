package uk.nhs.careconnect.ri.database.entity.referral;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="ReferralRequestIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_REFERRAL_IDENTIFIER", columnNames={"REFERRAL_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_REFERRAL_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class ReferralRequestIdentifier extends BaseIdentifier {

	public ReferralRequestIdentifier() {

	}

	public ReferralRequestIdentifier(ReferralRequestEntity referral) {
		this.referral = referral;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "REFERRAL_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "REFERRAL_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REFERRAL_IDENTIFIER"))
	private ReferralRequestEntity referral;


    public Long getIdentifierId() {
    	return identifierId;
    }

	public ReferralRequestEntity setIdentifierId(Long identifierId) {
    	this.identifierId = identifierId;
    	return this.getReferralRequest();
    }

	public ReferralRequestEntity getReferralRequest() {
	        return this.referral;
	}

	public ReferralRequestEntity setReferralRequest(ReferralRequestEntity referral) {
	        this.referral = referral;
	        return this.getReferralRequest();
	}




}
