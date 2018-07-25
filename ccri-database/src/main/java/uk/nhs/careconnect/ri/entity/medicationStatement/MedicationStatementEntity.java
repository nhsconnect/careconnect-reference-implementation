package uk.nhs.careconnect.ri.entity.medicationStatement;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.entity.medicationStatement.MedicationStatementDosage;
import uk.nhs.careconnect.ri.entity.medicationStatement.MedicationStatementIdentifier;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MedicationStatement")
public class MedicationStatementEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MEDICATION_STATEMENT_ID")
    private Long id;

    @OneToMany(mappedBy="statement", targetEntity = MedicationStatementIdentifier.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationStatementIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="statement", targetEntity = MedicationStatementBasedOn.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationStatementBasedOn> basedOn = new HashSet<>();

    @OneToMany(mappedBy="statement", targetEntity = MedicationStatementPartOf.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationStatementPartOf> partOfs = new HashSet<>();

    @ManyToOne
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_STATEMENT_ENCOUNTER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private EncounterEntity contextEncounter;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    MedicationStatement.MedicationStatementStatus status;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_CONCEPT",foreignKey= @ForeignKey(name="FK_STATEMENT_CATEGORY_CONCEPT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ConceptEntity categoryCode;

    @ManyToOne
    @JoinColumn (name = "MEDICATION_CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_STATEMENT_MEDICATION_CODE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private ConceptEntity medicationCode;

    @ManyToOne
    @JoinColumn (name ="MEDICATION_ID", nullable = true,foreignKey= @ForeignKey(name="FK_STATEMENT_MEDICATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private MedicationEntity medicationEntity;

    // Use start date for basic date, for periods populate end date
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effectiveStartDate")
    private Date effectiveStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effectiveEndDate")
    private Date effectiveEndDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "assertedDate")
    private Date assertedDate;

    @ManyToOne
    @JoinColumn (name = "INFORMATION_SOURCE_PATIENT_ID",foreignKey= @ForeignKey(name="FK_INFORMATION_PATIENT_MEDICATION_STATEMENT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity informationPatient;

    @ManyToOne
    @JoinColumn (name = "INFORMATION_SOURCE_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_INFORMATION_ORGANISATION_MEDICATION_STATEMENT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private OrganisationEntity informationOrganisation;

    @ManyToOne
    @JoinColumn (name = "INFORMATION_SOURCE_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_INFORMATION_PRACTITIONER_MEDICATION_STATEMENT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PractitionerEntity informationPractitioner;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_MEDICATION_STATEMENT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;


    @OneToMany(mappedBy="statement", targetEntity = MedicationStatementDerivedFrom.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationStatementDerivedFrom> derives = new HashSet<>();


    @ManyToOne
    @JoinColumn(name = "TAKEN_CONCEPT",foreignKey= @ForeignKey(name="FK_STATEMENT_TAKEN_CONCEPT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ConceptEntity takenCode;

    @ManyToOne
    @JoinColumn(name = "NOT_TAKEN_CONCEPT",foreignKey= @ForeignKey(name="FK_STATEMENT_NOT_TAKEN_CONCEPT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    ConceptEntity notTakenCode;

    @OneToMany(mappedBy="statement", targetEntity = MedicationStatementReason.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationStatementReason> reasons = new HashSet<>();

    @Column(name = "note", length=2048)
    String note;


    @OneToMany(mappedBy="statement", targetEntity = MedicationStatementDosage.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<MedicationStatementDosage> dosages = new HashSet<>();

    public Long getId() {
        return id;
    }

    public MedicationStatementEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public MedicationStatementEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Set<MedicationStatementIdentifier> getIdentifiers() {
        return identifiers;
    }

    public MedicationStatementEntity setIdentifiers(Set<MedicationStatementIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public MedicationStatementEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }

    public MedicationStatement.MedicationStatementStatus getStatus() {
        return status;

    }

    public MedicationStatementEntity setStatus(MedicationStatement.MedicationStatementStatus status) {
        this.status = status;
        return this;
    }

    public ConceptEntity getCategoryCode() {
        return categoryCode;
    }

    public MedicationStatementEntity setCategoryCode(ConceptEntity categoryCode) {
        this.categoryCode = categoryCode;
        return this;
    }

    public ConceptEntity getMedicationCode() {
        return medicationCode;
    }

    public MedicationStatementEntity setMedicationCode(ConceptEntity medicationCode) {
        this.medicationCode = medicationCode;
        return this;
    }

    public MedicationEntity getMedicationEntity() {
        return medicationEntity;
    }

    public MedicationStatementEntity setMedicationEntity(MedicationEntity medicationEntity) {
        this.medicationEntity = medicationEntity;
        return this;
    }

    public Date getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public MedicationStatementEntity setEffectiveStartDate(Date effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
        return this;
    }

    public Date getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public MedicationStatementEntity setEffectiveEndDate(Date effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
        return this;
    }

    public Date getAssertedDate() {
        return assertedDate;
    }

    public MedicationStatementEntity setAssertedDate(Date assertedDate) {
        this.assertedDate = assertedDate;
        return this;
    }

    public PatientEntity getInformationPatient() {
        return informationPatient;
    }

    public MedicationStatementEntity setInformationPatient(PatientEntity informationPatient) {
        this.informationPatient = informationPatient;
        return this;
    }

    public OrganisationEntity getInformationOrganisation() {
        return informationOrganisation;
    }

    public MedicationStatementEntity setInformationOrganisation(OrganisationEntity informationOrganisation) {
        this.informationOrganisation = informationOrganisation;
        return this;
    }

    public PractitionerEntity getInformationPractitioner() {
        return informationPractitioner;
    }

    public MedicationStatementEntity setInformationPractitioner(PractitionerEntity informationPractitioner) {
        this.informationPractitioner = informationPractitioner;
        return this;
    }

    public ConceptEntity getTakenCode() {
        return takenCode;
    }

    public MedicationStatementEntity setTakenCode(ConceptEntity takenCode) {
        this.takenCode = takenCode;
        return this;
    }

    public ConceptEntity getNotTakenCode() {
        return notTakenCode;
    }

    public MedicationStatementEntity setNotTakenCode(ConceptEntity notTakenCode) {
        this.notTakenCode = notTakenCode;
        return this;
    }

    public String getNote() {
        return note;
    }

    public MedicationStatementEntity setNote(String note) {
        this.note = note;

        return this;
    }

    public Set<MedicationStatementDosage> getDosages() {
        return dosages;
    }

    public MedicationStatementEntity setDosages(Set<MedicationStatementDosage> dosages) {
        this.dosages = dosages;
        return this;
    }

    public Set<MedicationStatementBasedOn> getBasedOn() {
        return basedOn;
    }

    public MedicationStatementEntity setBasedOn(Set<MedicationStatementBasedOn> basedOn) {
        this.basedOn = basedOn;
        return this;
    }

    public Set<MedicationStatementPartOf> getPartOfs() {
        return partOfs;
    }

    public MedicationStatementEntity setPartOfs(Set<MedicationStatementPartOf> partOfs) {
        this.partOfs = partOfs;
        return this;
    }

    public Set<MedicationStatementDerivedFrom> getDerives() {
        return derives;
    }

    public MedicationStatementEntity setDerives(Set<MedicationStatementDerivedFrom> derives) {
        this.derives = derives;
        return this;
    }

    public Set<MedicationStatementReason> getReasons() {
        return reasons;
    }

    public MedicationStatementEntity setReasons(Set<MedicationStatementReason> reasons) {
        this.reasons = reasons;
        return this;
    }
}
