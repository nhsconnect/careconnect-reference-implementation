package uk.nhs.careconnect.ri.entity.encounter;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "Encounter")
public class EncounterEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ENCOUNTER_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ENCOUNTER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;

    @OneToMany(mappedBy="encounter", targetEntity = EncounterEpisode.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<EncounterEpisode> episodes = new HashSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodStartDate")
    private Date periodStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodEndDate")
    private Date periodEndDate;

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

    public Set<EncounterEpisode> getEpisodes() {
        return episodes;
    }
    public void setEpisodes(Set<EncounterEpisode> episodes) {
        this.episodes = episodes;
    }

    public Date getPeriodEndDate() {
        return periodEndDate;
    }

    public Date getPeriodStartDate() {
        return periodStartDate;
    }

    public EncounterEntity setPeriodEndDate(Date periodEndDate) {
        this.periodEndDate = periodEndDate;
        return this;
    }

    public EncounterEntity setPeriodStartDate(Date periodStartDate) {
        this.periodStartDate = periodStartDate;
        return this;
    }
}
