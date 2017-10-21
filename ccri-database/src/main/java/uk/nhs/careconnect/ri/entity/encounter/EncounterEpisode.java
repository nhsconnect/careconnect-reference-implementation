package uk.nhs.careconnect.ri.entity.encounter;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;

import javax.persistence.*;

@Entity
@Table(name="EncounterEpisodeOfCare")
public class EncounterEpisode extends BaseResource {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ENCOUNTER_EPISODE_ID")
    private Long Id;

    @ManyToOne
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_EPISODE_ENCOUNTER"))
    private EncounterEntity encounter;

    @ManyToOne
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
