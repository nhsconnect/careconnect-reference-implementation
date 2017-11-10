package uk.nhs.careconnect.ri.entity.condition;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Condition_")
public class ConditionEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CONDITION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_CONDITION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_CONDITION_ENCOUNTER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private EncounterEntity contextEncounter;

    @ManyToOne
    @JoinColumn (name = "EPISODE_ID",foreignKey= @ForeignKey(name="FK_CONDITION_EPISODE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private EpisodeOfCareEntity contextEpisode;

    @ManyToOne
    @JoinColumn (name = "CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_CONDITION_CODE"))
    private ConceptEntity code;

    @ManyToOne
    @JoinColumn(name="CLINICAL_STATUS_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_CONDITION_STATUS_CONCEPT"))
    private ConceptEntity clinicalStatus;

    @OneToMany(mappedBy="condition", targetEntity=ConditionCategory.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private List<ConditionCategory> categories = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "assertedDateTime")
    private Date assertedDateTime;


    public Long getId() {
        return id;
    }

    public ConditionEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public ConditionEntity setCode(ConceptEntity code) {
        this.code = code;
        return this;
    }

    public ConditionEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public EpisodeOfCareEntity getContextEpisode() {
        return contextEpisode;
    }

    public ConditionEntity setContextEpisode(EpisodeOfCareEntity contextEpisode) {
        this.contextEpisode = contextEpisode;
        return this;
    }

    public ConditionEntity setCategories(List<ConditionCategory> categories) {
        this.categories = categories;
        return this;
    }

    public ConceptEntity getClinicalStatus() {
        return clinicalStatus;
    }

    public List<ConditionCategory> getCategories() {
        return categories;
    }

    public ConditionEntity setClinicalStatus(ConceptEntity clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
        return this;
    }

    public Date getAssertedDateTime() {
        return assertedDateTime;
    }

    public void setAssertedDateTime(Date recordedDateTime) {
        this.assertedDateTime = recordedDateTime;
    }
}
