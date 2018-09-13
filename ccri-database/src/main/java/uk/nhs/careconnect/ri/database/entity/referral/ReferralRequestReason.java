package uk.nhs.careconnect.ri.database.entity.referral;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;


import javax.persistence.*;

@Entity
@Table(name="ReferralRequestReason", uniqueConstraints= @UniqueConstraint(name="PK_REFERRAL_REASON", columnNames={"REFERRAL_REASON_ID"})
        ,indexes = { @Index(name="IDX_REFERRAL_REASON", columnList = "REASON_CONCEPT_ID")}
)
public class ReferralRequestReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "REFERRAL_REASON_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REFERRAL_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REASON_REFERRAL_ID"))
    private ReferralRequestEntity referral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REASON_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REASON_REASON_CONCEPT_ID"))
    private ConceptEntity reason;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getReason() {
        return reason;
    }

    public ReferralRequestEntity getReferralRequest() {
        return referral;
    }

    public void setReason(ConceptEntity reason) {
        this.reason = reason;
    }
    public void setReferralRequest(ReferralRequestEntity referral) {
        this.referral = referral;
    }
}
