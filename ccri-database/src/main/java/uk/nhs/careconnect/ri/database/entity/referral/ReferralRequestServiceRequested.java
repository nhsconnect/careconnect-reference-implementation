package uk.nhs.careconnect.ri.database.entity.referral;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ReferralRequestServiceRequested", uniqueConstraints= @UniqueConstraint(name="PK_REFERRAL_SERVICE", columnNames={"REFERRAL_SERVICE_ID"})
        ,indexes = { @Index(name="IDX_REFERRAL_SERVICE", columnList = "SERVICE_CONCEPT_ID")}
)
public class ReferralRequestServiceRequested {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "REFERRAL_SERVICE_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REFERRAL_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_SERVICE_ID"))
    private ReferralRequestEntity referral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SERVICE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_SERVICE_CONCEPT_ID"))
    private ConceptEntity service;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getService() {
        return service;
    }

    public ReferralRequestEntity getReferralRequest() {
        return referral;
    }

    public void setService(ConceptEntity service) {
        this.service = service;
    }
    public void setReferralRequest(ReferralRequestEntity referral) {
        this.referral = referral;
    }
}
