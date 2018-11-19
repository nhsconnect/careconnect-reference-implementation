package uk.nhs.careconnect.ri.database.entity.referral;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;

import javax.persistence.*;

@Entity
@Table(name="ReferralRequestReasonReference", uniqueConstraints= @UniqueConstraint(name="PK_REFERRAL_REASON_REFERENCE", columnNames={"REFERRAL_REASON_REFERENCE_ID"})
)
public class ReferralRequestReasonReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "REFERRAL_REASON_REFERENCE_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REFERRAL_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REASON__REFERENCE_REFERRAL_ID"))
    private ReferralRequestEntity referral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REASON_CONDITION_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REASON_REASON_CONDITION_ID"))
    private ConditionEntity condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REASON_OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REASON_REASON_OBSERVATION_ID"))
    private ObservationEntity observation;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }



    public ReferralRequestEntity getReferralRequest() {
        return referral;
    }


    public void setReferralRequest(ReferralRequestEntity referral) {
        this.referral = referral;
    }

    public ReferralRequestEntity getReferral() {
        return referral;
    }

    public void setReferral(ReferralRequestEntity referral) {
        this.referral = referral;
    }

    public ConditionEntity getCondition() {
        return condition;
    }

    public void setCondition(ConditionEntity condition) {
        this.condition = condition;
    }

    public ObservationEntity getObservation() {
        return observation;
    }

    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }
}
