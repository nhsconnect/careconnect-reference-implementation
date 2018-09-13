package uk.nhs.careconnect.ri.database.entity.allergy;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "AllergyIntolerance",
        indexes = {
                @Index(name = "IDX_ALLERGY_DATE", columnList="assertedDateTime"),
        })
public class AllergyIntoleranceEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ALLERGY_ID")
    private Long id;



    @Enumerated(EnumType.ORDINAL)
    @JoinColumn(name="clinicalStatus",foreignKey= @ForeignKey(name="FK_ALLERGY_CLINICAL_STATUS"))
    private AllergyIntolerance.AllergyIntoleranceClinicalStatus clinicalStatus;

    @Enumerated(EnumType.ORDINAL)
    @JoinColumn(name="verificationStatus")
    private AllergyIntolerance.AllergyIntoleranceVerificationStatus verificationStatus;

    @Column(name="TYPE_ID")
    @Enumerated(EnumType.ORDINAL)
    private AllergyIntolerance.AllergyIntoleranceType type;

    @OneToMany(mappedBy="allergy", targetEntity=AllergyIntoleranceCategory.class)

    private List<AllergyIntoleranceCategory> categories = new ArrayList<>();

    @Enumerated(EnumType.ORDINAL)
    @JoinColumn(name="criticality")
    private AllergyIntolerance.AllergyIntoleranceCriticality criticality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_CODE"))

    private ConceptEntity code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ALLERGY"))

    private PatientEntity patient;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "onsetDateTime")
    private Date onsetDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "assertedDateTime")
    private Date assertedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="RECORDER_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_RECORDER_PRACTITIONER_ID"))

    private PractitionerEntity recorderPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="RECORDER_PATIENT_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_RECORDER_PATIENT_ID"))

    private PatientEntity recorderPatient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ASSERTER_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_PRACTITIONER_ID"))

    private PractitionerEntity asserterPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ASSERTER_PATIENT_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_PATIENT_ID"))

    private PatientEntity asserterPatient;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastOccurenceDateTime")
    private Date lastOccurenceDateTime;
    
    @Column(name="note",length = 5000)
    private String note;

    @OneToMany(mappedBy="allergy", targetEntity=AllergyIntoleranceReaction.class)

    private List<AllergyIntoleranceReaction> reactions = new ArrayList<>();



    @OneToMany(mappedBy="allergy", targetEntity=AllergyIntoleranceIdentifier.class)

    private List<AllergyIntoleranceIdentifier> identifiers = new ArrayList<>();

    public EncounterEntity getAssociatedEncounter() {
        return associatedEncounter;
    }

    public void setAssociatedEncounter(EncounterEntity associatedEncounter) {
        this.associatedEncounter = associatedEncounter;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_ENCOUNTER"))

    private EncounterEntity associatedEncounter;
    
    public Long getId() {
        return id;
    }

    public AllergyIntoleranceEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }




    public AllergyIntolerance.AllergyIntoleranceClinicalStatus getClinicalStatus() {
        return clinicalStatus;
    }

    public AllergyIntoleranceEntity setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
        return this;
    }

    public AllergyIntolerance.AllergyIntoleranceVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public AllergyIntoleranceEntity setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
        return this;
    }

    public AllergyIntolerance.AllergyIntoleranceType getType() {
        return type;
    }

    public AllergyIntoleranceEntity setType(AllergyIntolerance.AllergyIntoleranceType type) {
        this.type = type;
        return this;
    }

    public AllergyIntolerance.AllergyIntoleranceCriticality getCriticality() {
        return criticality;
    }

    public AllergyIntoleranceEntity setCriticality(AllergyIntolerance.AllergyIntoleranceCriticality criticality) {
        this.criticality = criticality;
        return this;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public AllergyIntoleranceEntity setCode(ConceptEntity code) {
        this.code = code;
        return this;
    }

    public Date getOnsetDateTime() {
        return onsetDateTime;
    }

    public AllergyIntoleranceEntity setOnsetDateTime(Date onsetDateTime) {
        this.onsetDateTime = onsetDateTime;
        return this;
    }

    public Date getAssertedDateTime() {
        return assertedDateTime;
    }

    public AllergyIntoleranceEntity setAssertedDateTime(Date assertedDateTime) {
        this.assertedDateTime = assertedDateTime;
        return this;
    }

    public PractitionerEntity getRecorderPractitioner() {
        return recorderPractitioner;
    }

    public AllergyIntoleranceEntity setRecorderPractitioner(PractitionerEntity recorderPractitioner) {
        this.recorderPractitioner = recorderPractitioner;
        return this;
    }

    public PatientEntity getRecorderPatient() {
        return recorderPatient;
    }

    public AllergyIntoleranceEntity setRecorderPatient(PatientEntity recorderPatient) {
        this.recorderPatient = recorderPatient;
        return this;
    }

    public PractitionerEntity getAsserterPractitioner() {
        return asserterPractitioner;
    }

    public AllergyIntoleranceEntity setAsserterPractitioner(PractitionerEntity asserterPractitioner) {
        this.asserterPractitioner = asserterPractitioner;
        return this;
    }

    public PatientEntity getAsserterPatient() {
        return asserterPatient;
    }

    public AllergyIntoleranceEntity setAsserterPatient(PatientEntity asserterPatient) {
        this.asserterPatient = asserterPatient;
        return this;
    }

    public Date getLastOccurenceDateTime() {
        return lastOccurenceDateTime;
    }

    public AllergyIntoleranceEntity setLastOccurenceDateTime(Date lastOccurenceDateTime) {
        this.lastOccurenceDateTime = lastOccurenceDateTime;
        return this;
    }

    public String getNote() {
        return note;
    }

    public AllergyIntoleranceEntity setNote(String note) {
        this.note = note;
        return this;
    }

    public List<AllergyIntoleranceCategory> getCategories() {
        return categories;
    }

    public AllergyIntoleranceEntity setCategories(List<AllergyIntoleranceCategory> categories) {
        this.categories = categories;
        return this;
    }

    public List<AllergyIntoleranceReaction> getReactions() {
        return reactions;
    }

    public AllergyIntoleranceEntity setReactions(List<AllergyIntoleranceReaction> reactions) {
        this.reactions = reactions;
        return this;
    }

    public List<AllergyIntoleranceIdentifier> getIdentifiers() {
        return identifiers;
    }

    public AllergyIntoleranceEntity setIdentifiers(List<AllergyIntoleranceIdentifier> identifiers) {
        this.identifiers = identifiers;
        return  this;
    }


}
