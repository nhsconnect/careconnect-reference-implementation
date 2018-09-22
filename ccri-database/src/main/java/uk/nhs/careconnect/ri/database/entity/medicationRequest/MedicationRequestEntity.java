package uk.nhs.careconnect.ri.database.entity.medicationRequest;

import org.hl7.fhir.dstu3.model.MedicationRequest;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MedicationRequest",
        indexes = {
                @Index(name = "IDX_MEDICATION_REQUEST_DATE", columnList="authoredDate"),
        })
public class MedicationRequestEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRESCRIPTION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",nullable =false, foreignKey= @ForeignKey(name="FK_PATIENT_PRESCRIPTION"))

    private PatientEntity patient;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "MEDICATION_CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_PRESCRIPTION_MEDICATION_CODE"))
    private ConceptEntity medicationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name ="MEDICATION_ID", nullable = true,foreignKey= @ForeignKey(name="FK_PRESCRIPTION_MEDICATION"))

    private MedicationEntity medicationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_ENCOUNTER"))

    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EPISODE_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_EPISODE"))

    private EpisodeOfCareEntity contextEpisodeOfCare;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "writtenDate")
    private Date writtenDate;

    @OneToMany(mappedBy="prescription", targetEntity = MedicationRequestIdentifier.class)

    Set<MedicationRequestIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    MedicationRequest.MedicationRequestStatus status;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "intent", nullable = false)
    MedicationRequest.MedicationRequestIntent intent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_CATEGORY_CONCEPT"))

    ConceptEntity categoryCode;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "priority")
    MedicationRequest.MedicationRequestPriority priority;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "authoredDate")
    private Date authoredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORDER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_RECORDER_PRACTITIONER"))

    PractitionerEntity recorderPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REASON_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REASON_CONCEPT"))

    ConceptEntity reasonCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REASON_OBSERVATION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REASON_OBSERVATION"))

    ObservationEntity reasonObservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REASON_CONDITION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REASON_CONDITION"))

    ConditionEntity reasonCondition;

    @Column(name = "substitutionAllowed")
    Boolean substitutionAllowed;

    @OneToMany(mappedBy="prescription", targetEntity = MedicationRequestDosage.class)

    Set<MedicationRequestDosage> dosages = new HashSet<>();

    public ConceptEntity getSupplyTypeCode() {
        return supplyTypeCode;
    }

    public void setSupplyTypeCode(ConceptEntity supplyTypeCode) {
        this.supplyTypeCode = supplyTypeCode;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPPLY_TYPE_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_SUPPLY_TYPE_CONCEPT"))

    ConceptEntity supplyTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_PRACTITIONER"))

    PractitionerEntity requesterPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_ORGANISATION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_ORGANISATION"))

    OrganisationEntity requesterOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_PATIENT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_PATIENT"))

    PatientEntity requesterPatient;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_ONBEHALF_ORGANISATION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_ONBEHALF_ORGANISATION"))

    OrganisationEntity requesterOnBehalfOfOrganisation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dispenseRequestStart")
    private Date dispenseRequestStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dispenseRequestEnd")
    private Date dispenseRequestEnd;

    @Column(name="numberOfRepeatsAllowed")
    private Integer numberOfRepeatsAllowed;


    @Column(name="expectedSupplyDuration")
    private BigDecimal expectedSupplyDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DURATION_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DURATION_UNITS_CONCEPT"))

    ConceptEntity durationUnitsCode;

    public Date getDispenseRequestStart() {
        return dispenseRequestStart;
    }

    public MedicationRequestEntity setDispenseRequestStart(Date dispenseRequestStart) {
        this.dispenseRequestStart = dispenseRequestStart;
        return this;
    }

    public Date getDispenseRequestEnd() {
        return dispenseRequestEnd;
    }

    public MedicationRequestEntity setDispenseRequestEnd(Date dispenseRequestEnd) {
        this.dispenseRequestEnd = dispenseRequestEnd;
        return this;
    }

    public Integer getNumberOfRepeatsAllowed() {
        return numberOfRepeatsAllowed;
    }

    public MedicationRequestEntity setNumberOfRepeatsAllowed(Integer numberOfRepeatsAllowed) {
        this.numberOfRepeatsAllowed = numberOfRepeatsAllowed;
        return this;
    }

    public BigDecimal getExpectedSupplyDuration() {
        return expectedSupplyDuration;
    }

    public MedicationRequestEntity setExpectedSupplyDuration(BigDecimal expectedSupplyDuration) {
        this.expectedSupplyDuration = expectedSupplyDuration;
        return this;
    }

    public ConceptEntity getDurationUnitsCode() {
        return durationUnitsCode;
    }

    public MedicationRequestEntity setDurationUnitsCode(ConceptEntity durationUnitsCode) {
        this.durationUnitsCode = durationUnitsCode;
        return this;
    }

    public Set<MedicationRequestIdentifier> getIdentifiers() {
        return identifiers;
    }

    public MedicationRequestEntity setIdentifiers(Set<MedicationRequestIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public PractitionerEntity getRequesterPractitioner() {
        return requesterPractitioner;
    }

    public MedicationRequestEntity setRequesterPractitioner(PractitionerEntity requesterPractitioner) {
        this.requesterPractitioner = requesterPractitioner;
        return this;
    }

    public OrganisationEntity getRequesterOrganisation() {
        return requesterOrganisation;
    }

    public MedicationRequestEntity setRequesterOrganisation(OrganisationEntity requesterOrganisation) {
        this.requesterOrganisation = requesterOrganisation;
        return this;
    }

    public PatientEntity getRequesterPatient() {
        return requesterPatient;
    }

    public MedicationRequestEntity setRequesterPatient(PatientEntity requesterPatient) {
        this.requesterPatient = requesterPatient;
        return this;
    }

    public OrganisationEntity getRequesterOnBehalfOfOrganisation() {
        return requesterOnBehalfOfOrganisation;
    }

    public MedicationRequestEntity setRequesterOnBehalfOfOrganisation(OrganisationEntity requesterOnBehalfOfOrganisation) {
        this.requesterOnBehalfOfOrganisation = requesterOnBehalfOfOrganisation;
        return this;
    }

    public Long getId() {
        return id;
    }

    public MedicationRequestEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }



    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public MedicationRequestEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }

 
    public Date getWrittenDate() {
        return writtenDate;
    }

    public MedicationRequestEntity setWrittenDate(Date writtenDate) {
        this.writtenDate = writtenDate;
        return this;
    }

    public EpisodeOfCareEntity getContextEpisodeOfCare() {
        return contextEpisodeOfCare;
    }

    public MedicationRequestEntity setContextEpisodeOfCare(EpisodeOfCareEntity contextEpisodeOfCare) {
        this.contextEpisodeOfCare = contextEpisodeOfCare;
        return this;
    }

    public MedicationRequest.MedicationRequestStatus getStatus() {
        return status;
    }

    public MedicationRequestEntity setStatus(MedicationRequest.MedicationRequestStatus status) {
        this.status = status;
        return this;
    }

    public MedicationRequest.MedicationRequestIntent getIntent() {
        return intent;
    }

    public MedicationRequestEntity setIntent(MedicationRequest.MedicationRequestIntent intent) {
        this.intent = intent;
        return this;
    }

    public ConceptEntity getCategoryCode() {
        return categoryCode;
    }

    public MedicationRequestEntity setCategoryCode(ConceptEntity categoryCode) {
        this.categoryCode = categoryCode;
        return this;
    }

    public MedicationRequest.MedicationRequestPriority getPriority() {
        return priority;
    }

    public MedicationRequestEntity setPriority(MedicationRequest.MedicationRequestPriority priority) {
        this.priority = priority;
        return this;
    }

    public Date getAuthoredDate() {
        return authoredDate;
    }

    public MedicationRequestEntity setAuthoredDate(Date authoredDate) {
        this.authoredDate = authoredDate;
        return this;
    }

    public PractitionerEntity getRecorderPractitioner() {
        return recorderPractitioner;
    }

    public MedicationRequestEntity setRecorderPractitioner(PractitionerEntity recorderPractitioner) {
        this.recorderPractitioner = recorderPractitioner;
        return this;
    }

    public ConceptEntity getReasonCode() {
        return reasonCode;
    }

    public MedicationRequestEntity setReasonCode(ConceptEntity reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    public ObservationEntity getReasonObservation() {
        return reasonObservation;
    }

    public MedicationRequestEntity setReasonObservation(ObservationEntity reasonObservation) {
        this.reasonObservation = reasonObservation;
        return this;
    }

    public ConditionEntity getReasonCondition() {
        return reasonCondition;
    }

    public MedicationRequestEntity setReasonCondition(ConditionEntity reasonCondition) {
        this.reasonCondition = reasonCondition;
        return this;
    }

    public Boolean getSubstitutionAllowed() {
        return substitutionAllowed;
    }

    public MedicationRequestEntity setSubstitutionAllowed(Boolean substitutionAllowed) {
        this.substitutionAllowed = substitutionAllowed;
        return this;
    }

    public Set<MedicationRequestDosage> getDosages() {
        return dosages;
    }

    public MedicationRequestEntity setDosages(Set<MedicationRequestDosage> dosages) {
        this.dosages = dosages;
        return this;
    }

    public MedicationEntity getMedicationEntity() {
        return medicationEntity;
    }

    public void setMedicationEntity(MedicationEntity medicationEntity) {
        this.medicationEntity = medicationEntity;
    }


}
