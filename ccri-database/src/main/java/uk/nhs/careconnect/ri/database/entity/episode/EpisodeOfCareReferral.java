package uk.nhs.careconnect.ri.database.entity.episode;

import uk.nhs.careconnect.ri.database.entity.referral.ReferralRequestEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;

@Entity
@Table(name="EpisodeOfCareReferral")
public class EpisodeOfCareReferral extends BaseResource {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "EPISODE_REFERRAL_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "EPISODE_ID",foreignKey= @ForeignKey(name="FK_EPISODE_REFERRAL_EPISODE"))
    private EpisodeOfCareEntity episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REFERRAL_ID",foreignKey= @ForeignKey(name="FK_EPISODE_REFERRAL_REFERRAL"))
    private ReferralRequestEntity referral;

    public Long getId() {
        return Id;
    }

    public EpisodeOfCareEntity getEpisode() {
        return episode;
    }

    public EpisodeOfCareReferral setEpisode(EpisodeOfCareEntity episode) {
        this.episode = episode;
        return this;
    }

    public ReferralRequestEntity getReferral() {
        return referral;
    }

    public EpisodeOfCareReferral setReferral(ReferralRequestEntity referral) {
        this.referral = referral;
        return this;
    }
}
