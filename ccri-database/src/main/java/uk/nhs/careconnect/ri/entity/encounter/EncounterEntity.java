package uk.nhs.careconnect.ri.entity.encounter;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Encounter")
public class EncounterEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ENCOUNTER_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ENCOUNTER"))
    private PatientEntity patient;

    @OneToMany(mappedBy="encounter", targetEntity = EncounterEpisode.class)
    List<EncounterEpisode> episodes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public EncounterEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public List<EncounterEpisode> getEpisodes() {
        return episodes;
    }
    public void setEpisodes(List<EncounterEpisode> episodes) {
        this.episodes = episodes;
    }
}
