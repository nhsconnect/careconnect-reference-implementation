package uk.nhs.careconnect.ri.entity.medication;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionIdentifier;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MedicationRequest")
public class MedicationRequestEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRESCRIPTION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PRESCRIPTION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "MEDICATION_CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_MEDICATION_CODE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private ConceptEntity medicationCode;

    @ManyToOne
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_ENCOUNTER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private EncounterEntity contextEncounter;

    @ManyToOne
    @JoinColumn(name="EPISODE_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_EPISODE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private EncounterEntity contextEpisodeOfCare;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "writtenDate")
    private Date writtenDate;

    @OneToMany(mappedBy="prescription", targetEntity = MedicationRequestIdentifier.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationRequestIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    MedicationRequest.MedicationRequestStatus status;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "intent")
    MedicationRequest.MedicationRequestIntent intent;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_CATEGORY_CONCEPT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ConceptEntity categoryCode;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "priority")
    MedicationRequest.MedicationRequestPriority priority;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "authoredDate")
    private Date authoredDate;

    @ManyToOne
    @JoinColumn(name = "RECORDER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_RECORDER_PRACTITIONER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    PractitionerEntity recorderPractitioner;

    @ManyToOne
    @JoinColumn(name = "REASON_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REASON_CONCEPT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ConceptEntity reasonCode;

    @ManyToOne
    @JoinColumn(name = "REASON_OBSERVATION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REASON_OBSERVATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ObservationEntity reasonObservation;

    @ManyToOne
    @JoinColumn(name = "REASON_CONDITION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REASON_CONDITION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ConditionEntity reasonCondition;

    @Column(name = "substitutionAllowed")
    Boolean substitutionAllowed;

    @OneToMany(mappedBy="prescription", targetEntity = MedicationRequestDosage.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationRequestDosage> dosages = new HashSet<>();

    public ConceptEntity getSupplyTypeCode() {
        return supplyTypeCode;
    }

    public void setSupplyTypeCode(ConceptEntity supplyTypeCode) {
        this.supplyTypeCode = supplyTypeCode;
    }

    @ManyToOne
    @JoinColumn(name = "SUPPLY_TYPE_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_SUPPLY_TYPE_CONCEPT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ConceptEntity supplyTypeCode;

    @ManyToOne
    @JoinColumn(name = "REQUESTER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_PRACTITIONER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    PractitionerEntity requesterPractitioner;

    @ManyToOne
    @JoinColumn(name = "REQUESTER_ORGANISATION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_ORGANISATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    OrganisationEntity requesterOrganisation;

    @ManyToOne
    @JoinColumn(name = "REQUESTER_PATIENT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_PATIENT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    PatientEntity requesterPatient;


    @ManyToOne
    @JoinColumn(name = "REQUESTER_ONBEHALF_ORGANISATION",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_REQUESTER_ONBEHALF_ORGANISATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    OrganisationEntity requesterOnBehalfOfOrganisation;


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

    public ConceptEntity getMedicationCode() {
        return medicationCode;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public MedicationRequestEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }

    public MedicationRequestEntity setMedicationCode(ConceptEntity medicationCode) {
        this.medicationCode = medicationCode;
        return this;
    }
 
    public Date getWrittenDate() {
        return writtenDate;
    }

    public MedicationRequestEntity setWrittenDate(Date writtenDate) {
        this.writtenDate = writtenDate;
        return this;
    }

    public EncounterEntity getContextEpisodeOfCare() {
        return contextEpisodeOfCare;
    }

    public MedicationRequestEntity setContextEpisodeOfCare(EncounterEntity contextEpisodeOfCare) {
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
}
