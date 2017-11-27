package uk.nhs.careconnect.ri.entity.condition;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.Condition;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.IBaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.*;

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
    @LazyCollection(LazyCollectionOption.TRUE)
    private ConceptEntity code;

    @ManyToOne
    @JoinColumn (name = "SEVERITY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_CONDITION_SEVERITY_CODE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private ConceptEntity severity;

    @Enumerated(EnumType.ORDINAL)
    @JoinColumn(name="CLINICAL_STATUS_CONCEPT_ID")
    private Condition.ConditionClinicalStatus clinicalStatus;

    @OneToMany(mappedBy="condition", targetEntity=ConditionCategory.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private List<ConditionCategory> categories = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "assertedDateTime")
    private Date assertedDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "onsetDateTime")
    private Date onsetDateTime;

    @ManyToOne
    @JoinColumn(name="ASSERTER_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_CONDITION_PRACTITIONER_ID"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PractitionerEntity asserterPractitioner;

    @Enumerated(EnumType.ORDINAL)
    private Condition.ConditionVerificationStatus verificationStatus;

    @OneToMany(mappedBy="condition", targetEntity = ConditionIdentifier.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<ConditionIdentifier> identifiers = new HashSet<>();

    public Set<ConditionIdentifier> getIdentifiers() {
        return identifiers;
    }

    public ConditionEntity setIdentifiers(Set<ConditionIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

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

    public Condition.ConditionClinicalStatus getClinicalStatus() {
        return clinicalStatus;
    }

    public List<ConditionCategory> getCategories() {
        return categories;
    }

    public ConditionEntity setClinicalStatus(Condition.ConditionClinicalStatus clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
        return this;
    }

    public Date getAssertedDateTime() {
        return assertedDateTime;
    }

    public ConditionEntity setAssertedDateTime(Date recordedDateTime) {
        this.assertedDateTime = recordedDateTime;
        return this;
    }

    public Date getOnsetDateTime() {
        return onsetDateTime;
    }

    public ConditionEntity setOnsetDateTime(Date onsetDateTime) {
        this.onsetDateTime = onsetDateTime;
        return this;
    }

    public PractitionerEntity getAsserterPractitioner() {
        return asserterPractitioner;
    }

    public Condition.ConditionVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public ConditionEntity setAsserterPractitioner(PractitionerEntity asserterPractitioner) {
        this.asserterPractitioner = asserterPractitioner;
        return this;
    }

    public ConditionEntity setVerificationStatus(Condition.ConditionVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
        return this;
    }

    public ConceptEntity getSeverity() {
        return severity;
    }

    public ConditionEntity setSeverity(ConceptEntity severity) {
        this.severity = severity;
        return this;
    }


}
