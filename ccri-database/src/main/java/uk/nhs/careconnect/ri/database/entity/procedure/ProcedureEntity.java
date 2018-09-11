package uk.nhs.careconnect.ri.database.entity.procedure;

import org.hl7.fhir.dstu3.model.Procedure;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.referral.ReferralRequestEntity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "Procedure_",
        indexes = {
                @Index(name = "IDX_PROCEDURE_DATE", columnList="performedDate"),
        })
public class ProcedureEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PROCEDURE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "BASED_ON_REFERRAL_REQUEST_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_REFERRAL_ID"))
    private ReferralRequestEntity basedOnReferral;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "PART_OF_PROCEDURE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_PROCEDURE"))
    private ProcedureEntity partOfProcedure;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status", nullable = false)
    private Procedure.ProcedureStatus status;


    @Column(name = "notDone")
    private Boolean notDone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "NOT_DONE_REASON_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_NOT_DONE_CODE"))

    private ConceptEntity notDoneReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CATEGORY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_CATEGORY_CODE"))

    private ConceptEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CODE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_CODE"))

    private ConceptEntity code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",nullable=false,foreignKey= @ForeignKey(name="FK_PATIENT_PROCEDURE"))

    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_ENCOUNTER"))

    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "EPISODE_OF_CARE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_EPISODE_OF_CARE"))

    private EpisodeOfCareEntity contextEpisode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "performedDate")
    private Date performedDate;

    public Date getPerformedEndDate() {
        return performedEndDate;
    }

    public void setPerformedEndDate(Date performedEndDate) {
        this.performedEndDate = performedEndDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "performedEndDate")
    private Date performedEndDate;

    @OneToMany(mappedBy="procedure", targetEntity=ProcedurePerformer.class)

    private List<ProcedurePerformer> performers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROCEDURE_LOCATION_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_LOCATION"))

    private LocationEntity location;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "REASON_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_REASON_CONCEPT"))
    private ConceptEntity reason;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "REASON_OBSERVATION",foreignKey= @ForeignKey(name="FK_PROCEDURE_REASON_OBSERVATION"))
    private ObservationEntity reasonObservation;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "REASON_CONDITION",foreignKey= @ForeignKey(name="FK_PROCEDURE_REASON_CONDITION"))
    private ConditionEntity reasonCondition;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "BODY_SITE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_BODY_SITE_CONCEPT"))
    private ConceptEntity bodySite;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "OUTCOME_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_OUTCOME_CONCEPT"))
    private ConceptEntity outcome;

    @OneToMany(mappedBy="procedure", targetEntity=ProcedureIdentifier.class)

    private Set<ProcedureIdentifier> identifiers = new HashSet<>();

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

    public Set<ProcedureIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<ProcedureIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
}
