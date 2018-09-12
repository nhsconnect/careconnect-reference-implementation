package uk.nhs.careconnect.ri.database.entity.encounter;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;

import javax.persistence.*;

@Entity
@Table(name="EncounterEpisodeOfCare" ,indexes = {

        @Index(name="IDX_ENCOUNTER_EPISODE_ENCOUNTER_ID", columnList = "ENCOUNTER_ID")
})
public class EncounterEpisode extends BaseResource {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ENCOUNTER_EPISODE_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_EPISODE_ENCOUNTER"))
    private EncounterEntity encounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "EPISODE_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_EPISODE_EPISODE"))

    private EpisodeOfCareEntity episode;



    public Long getId() {
        return Id;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public EpisodeOfCareEntity getEpisode() {
        return episode;
    }

    public EncounterEpisode setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
        return this;
    }

    public EncounterEpisode setEpisode(EpisodeOfCareEntity episode) {
        this.episode = episode;
        return this;
    }


}
