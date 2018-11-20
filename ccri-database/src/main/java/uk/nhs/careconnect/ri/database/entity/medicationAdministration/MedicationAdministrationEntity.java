package uk.nhs.careconnect.ri.database.entity.medicationAdministration;

import org.hl7.fhir.dstu3.model.MedicationAdministration;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MedicationAdministration")
public class MedicationAdministrationEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ADMINISTRATION_ID")
    private Long id;

    @OneToMany(mappedBy="administration", targetEntity = MedicationAdministrationIdentifier.class)
    Set<MedicationAdministrationIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="administration", targetEntity = MedicationAdministrationPartOf.class)
    Set<MedicationAdministrationPartOf> partOfs = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    MedicationAdministration.MedicationAdministrationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_CATEGORY_CONCEPT"))
    ConceptEntity categoryCode;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "MEDICATION_CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_ADMINISTRATION_MEDICATION_CODE"))
    private ConceptEntity medicationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name ="MEDICATION_ID", nullable = true,foreignKey= @ForeignKey(name="FK_ADMINISTRATION_MEDICATION"))
    private MedicationEntity medicationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",nullable =false, foreignKey= @ForeignKey(name="FK_PATIENT_ADMINISTRATION"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_ENCOUNTER"))
    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EPISODE_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_EPISODE"))
    private EpisodeOfCareEntity contextEpisodeOfCare;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EFFECTIVE_DATE_START")
    private Date effectiveStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EFFECTIVE_DATE_END")
    private Date effectiveEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERFORMER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_PERFORMER_PRACTITIONER"))
    PractitionerEntity performerPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERFORMER_ORGANISATION",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_PERFORMER_ORGANISATION"))
    OrganisationEntity performerOrganisation;

    @Column(name="quantityValue")
    private Boolean notGiven;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REASON_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_REASON_CONCEPT"))
    ConceptEntity reasonCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTGIVEN_REASON_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_NOTGIVEN_REASON_CONCEPT"))
    ConceptEntity reasonNotGivenCode;

    @OneToMany(mappedBy="administration", targetEntity = MedicationAdministrationReason.class)
    Set<MedicationAdministrationReason> reasons = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PRESCRIPTION_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_PRECRIPTION_ID"))
    private MedicationRequestEntity prescription;


    @Column(name = "note")
    String note;

    @OneToMany(mappedBy="administration", targetEntity = MedicationAdministrationDosage.class)
    Set<MedicationAdministrationDosage> dosages = new HashSet<>();




    public Set<MedicationAdministrationIdentifier> getIdentifiers() {
        return identifiers;
    }

    public MedicationAdministrationEntity setIdentifiers(Set<MedicationAdministrationIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public Long getId() {
        return id;
    }

    public MedicationAdministrationEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }


    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public MedicationAdministrationEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }



    public EpisodeOfCareEntity getContextEpisodeOfCare() {
        return contextEpisodeOfCare;
    }

    public MedicationAdministrationEntity setContextEpisodeOfCare(EpisodeOfCareEntity contextEpisodeOfCare) {
        this.contextEpisodeOfCare = contextEpisodeOfCare;
        return this;
    }

    public MedicationAdministration.MedicationAdministrationStatus getStatus() {
        return status;
    }

    public MedicationAdministrationEntity setStatus(MedicationAdministration.MedicationAdministrationStatus status) {
        this.status = status;
        return this;
    }

    public ConceptEntity getCategoryCode() {
        return categoryCode;
    }

    public MedicationAdministrationEntity setCategoryCode(ConceptEntity categoryCode) {
        this.categoryCode = categoryCode;
        return this;
    }

    public MedicationEntity getMedicationEntity() {
        return medicationEntity;
    }

    public void setMedicationEntity(MedicationEntity medicationEntity) {
        this.medicationEntity = medicationEntity;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PractitionerEntity getPerformerPractitioner() {
        return performerPractitioner;
    }

    public void setPerformerPractitioner(PractitionerEntity performerPractitioner) {
        this.performerPractitioner = performerPractitioner;
    }

    public OrganisationEntity getPerformerOrganisation() {
        return performerOrganisation;
    }

    public void setPerformerOrganisation(OrganisationEntity performerOrganisation) {
        this.performerOrganisation = performerOrganisation;
    }

    public MedicationRequestEntity getPrescription() {
        return prescription;
    }

    public void setPrescription(MedicationRequestEntity prescription) {
        this.prescription = prescription;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Set<MedicationAdministrationPartOf> getPartOfs() {
        return partOfs;
    }

    public void setPartOfs(Set<MedicationAdministrationPartOf> partOfs) {
        this.partOfs = partOfs;
    }

    public ConceptEntity getMedicationCode() {
        return medicationCode;
    }

    public void setMedicationCode(ConceptEntity medicationCode) {
        this.medicationCode = medicationCode;
    }

    public Date getEffectiveStart() {
        return effectiveStart;
    }

    public void setEffectiveStart(Date effectiveStart) {
        this.effectiveStart = effectiveStart;
    }

    public Date getEffectiveEnd() {
        return effectiveEnd;
    }

    public void setEffectiveEnd(Date effectiveEnd) {
        this.effectiveEnd = effectiveEnd;
    }

    public Boolean getNotGiven() {
        return notGiven;
    }

    public void setNotGiven(Boolean notGiven) {
        this.notGiven = notGiven;
    }

    public ConceptEntity getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(ConceptEntity reasonCode) {
        this.reasonCode = reasonCode;
    }

    public ConceptEntity getReasonNotGivenCode() {
        return reasonNotGivenCode;
    }

    public void setReasonNotGivenCode(ConceptEntity reasonNotGivenCode) {
        this.reasonNotGivenCode = reasonNotGivenCode;
    }

    public Set<MedicationAdministrationReason> getReasons() {
        return reasons;
    }

    public void setReasons(Set<MedicationAdministrationReason> reasons) {
        this.reasons = reasons;
    }

    public Set<MedicationAdministrationDosage> getDosages() {
        return dosages;
    }

    public void setDosages(Set<MedicationAdministrationDosage> dosages) {
        this.dosages = dosages;
    }
}
