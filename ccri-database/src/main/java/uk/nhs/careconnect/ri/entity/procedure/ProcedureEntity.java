package uk.nhs.careconnect.ri.entity.procedure;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.Procedure;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.referral.ReferralRequestEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Procedure_")
public class ProcedureEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PROCEDURE_ID")
    private Long id;




    @ManyToOne
    @JoinColumn (name = "BASED_ON_REFERRAL_REQUEST_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_REFERRAL_ID"))
    private ReferralRequestEntity basedOnReferral;

    @ManyToOne
    @JoinColumn (name = "PART_OF_PROCEDURE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_PROCEDURE"))
    private ProcedureEntity partOfProcedure;

    @Enumerated(EnumType.ORDINAL)
    private Procedure.ProcedureStatus status;


    @Column(name = "notDone")
    private Boolean notDone;

    @ManyToOne
    @JoinColumn (name = "NOT_DONE_REASON_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_NOT_DONE_CODE"))
    private ConceptEntity notDoneReason;

    @ManyToOne
    @JoinColumn (name = "CATEGORY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_CATEGORY_CODE"))
    private ConceptEntity category;

    @ManyToOne
    @JoinColumn (name = "CODE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_CODE"))
    private ConceptEntity code;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PROCEDURE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_ENCOUNTER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private EncounterEntity contextEncounter;

    @ManyToOne
    @JoinColumn (name = "EPISODE_OF_CARE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_EPISODE_OF_CARE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private EpisodeOfCareEntity contextEpisode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "performedDate")
    private Date performedDate;


    @OneToMany(mappedBy="procedure", targetEntity=ProcedurePerformer.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private List<ProcedurePerformer> performers = new ArrayList<>();

    @ManyToOne
    @JoinColumn (name = "PROCEDURE_LOCATION_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_LOCATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private LocationEntity location;

    @ManyToOne
    @JoinColumn (name = "REASON_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_REASON_CONCEPT"))
    private ConceptEntity reason;

    @ManyToOne
    @JoinColumn (name = "REASON_OBSERVATION",foreignKey= @ForeignKey(name="FK_PROCEDURE_REASON_OBSERVATION"))
    private ObservationEntity reasonObservation;

    @ManyToOne
    @JoinColumn (name = "REASON_CONDITION",foreignKey= @ForeignKey(name="FK_PROCEDURE_REASON_CONDITION"))
    private ConditionEntity reasonCondition;

    @ManyToOne
    @JoinColumn (name = "BODY_SITE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_BODY_SITE_CONCEPT"))
    private ConceptEntity bodySite;

    @ManyToOne
    @JoinColumn (name = "OUTCOME_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_OUTCOME_CONCEPT"))
    private ConceptEntity outcome;


    public Long getId() {
        return id;
    }

    public ProcedureEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public EncounterEntity getEncounter() {
        return contextEncounter;
    }

    public ProcedureEntity setEncounter(EncounterEntity encounter) {
        this.contextEncounter = encounter;
        return this;
    }

    public ProcedureEntity setCode(ConceptEntity code) {
        this.code = code;
        return this;
    }

    public Date getPerformedDate() {
        return performedDate;
    }

    public ProcedureEntity setPerformedDate(Date performedDate) {
        this.performedDate = performedDate;
        return this;
    }

    public ProcedureEntity setStatus(Procedure.ProcedureStatus status) {
        this.status = status;
        return this;
    }

    public ProcedureEntity setLocation(LocationEntity location) {
        this.location = location;
        return this;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public ConceptEntity getReason() {
        return reason;
    }

    public ProcedureEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public EpisodeOfCareEntity getContextEpisode() {
        return contextEpisode;
    }

    public ProcedureEntity setPerformers(List<ProcedurePerformer> performers) {
        this.performers = performers;
        return this;
    }

    public ProcedureEntity setCategory(ConceptEntity category) {
        this.category = category;
        return this;
    }

    public ReferralRequestEntity getBasedOnReferral() {
        return basedOnReferral;
    }

    public ProcedureEntity setBasedOnReferral(ReferralRequestEntity basedOnReferral) {
        this.basedOnReferral = basedOnReferral;
        return this;
    }

    public ProcedureEntity getPartOfProcedure() {
        return partOfProcedure;
    }

    public ProcedureEntity setPartOfProcedure(ProcedureEntity partOfProcedure) {
        this.partOfProcedure = partOfProcedure;
        return this;
    }

    public Procedure.ProcedureStatus getStatus() {
        return status;
    }

    public Boolean getNotDone() {
        return notDone;
    }

    public ProcedureEntity setNotDone(Boolean notDone) {
        this.notDone = notDone;
        return this;
    }

    public ConceptEntity getNotDoneReason() {
        return notDoneReason;
    }

    public ProcedureEntity setNotDoneReason(ConceptEntity notDoneReason) {
        this.notDoneReason = notDoneReason;
        return this;
    }

    public ConceptEntity getCategory() {
        return category;
    }

    public ProcedureEntity setContextEpisode(EpisodeOfCareEntity contextEpisode) {
        this.contextEpisode = contextEpisode;
        return this;
    }

    public List<ProcedurePerformer> getPerformers() {
        return performers;
    }

    public ProcedureEntity setReason(ConceptEntity reason) {
        this.reason = reason;
        return this;
    }

    public ObservationEntity getReasonObservation() {
        return reasonObservation;
    }

    public ProcedureEntity setReasonObservation(ObservationEntity reasonObservation) {
        this.reasonObservation = reasonObservation;
        return this;
    }

    public ConditionEntity getReasonCondition() {
        return reasonCondition;
    }

    public ProcedureEntity setReasonCondition(ConditionEntity reasonCondition) {
        this.reasonCondition = reasonCondition;
        return this;
    }

    public ConceptEntity getBodySite() {
        return bodySite;
    }

    public ProcedureEntity setBodySite(ConceptEntity bodySite) {
        this.bodySite = bodySite;
        return this;
    }

    public ConceptEntity getOutcome() {
        return outcome;
    }

    public ProcedureEntity setOutcome(ConceptEntity outcome) {
        this.outcome = outcome;
        return this;
    }
}
