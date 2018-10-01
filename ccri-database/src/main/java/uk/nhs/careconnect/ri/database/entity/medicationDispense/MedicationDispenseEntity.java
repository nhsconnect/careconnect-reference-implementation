package uk.nhs.careconnect.ri.database.entity.medicationDispense;

import org.hl7.fhir.dstu3.model.MedicationDispense;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MedicationDispense",
        indexes = {
                @Index(name = "IDX_MEDICATION_HANDED_OVER_DATE", columnList="whenHandedOver"),
        })
public class MedicationDispenseEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="DISPENSE_ID")
    private Long id;

    @OneToMany(mappedBy="dispense", targetEntity = MedicationDispenseIdentifier.class)
    Set<MedicationDispenseIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    MedicationDispense.MedicationDispenseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_CATEGORY_CONCEPT"))
    ConceptEntity categoryCode;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "MEDICATION_CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_DISPENSE_MEDICATION_CODE"))
    private ConceptEntity medicationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name ="MEDICATION_ID", nullable = true,foreignKey= @ForeignKey(name="FK_DISPENSE_MEDICATION"))
    private MedicationEntity medicationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",nullable =false, foreignKey= @ForeignKey(name="FK_PATIENT_DISPENSE"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_ENCOUNTER"))
    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EPISODE_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_EPISODE"))
    private EpisodeOfCareEntity contextEpisodeOfCare;

    // Supporting Information

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERFORMER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_DISPENSE_PERFORMER_PRACTITIONER"))
    PractitionerEntity performerPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERFORMER_ORGANISATION",foreignKey= @ForeignKey(name="FK_DISPENSE_PERFORMER_ORGANISATION"))
    OrganisationEntity performerOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PRESCRIPTION_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_PRECRIPTION_ID"))
    private MedicationRequestEntity prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_TYPE_CONCEPT"))
    ConceptEntity typeCode;

    @Column(name="quantityValue")
    private BigDecimal quantityValue;

    @Column(name="quantityUnit")
    private String quantityUnit;

    @Column(name="daysSupplyValue")
    private BigDecimal daysSupplyValue;

    @Column(name="daysSupplyUnit")
    private String daysSupplyUnit;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "whenPrepared")
    private Date whenPrepared;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "whenHandedOver")
    private Date whenHandedOver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_LOCATION_ID"))
    LocationEntity location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_DISPENSE_RECEIVER_PRACTITIONER"))
    PractitionerEntity receiverPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVER_ORGANISATION",foreignKey= @ForeignKey(name="FK_DISPENSE_RECEIVER_ORGANISATION"))
    OrganisationEntity receiverOrganisaton;

    @Column(name = "note")
    String note;

    @OneToMany(mappedBy="dispense", targetEntity = MedicationDispenseDosage.class)
    Set<MedicationDispenseDosage> dosageInstructions = new HashSet<>();

    @Column(name = "substituted")
    Boolean substituted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBSTITUTION_TYPE_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_SUBSTITUTION_TYPE_CONCEPT"))
    ConceptEntity substitutionTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBSTITUTION_REASON_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_SUBSTITUTION_REASON_CONCEPT"))
    ConceptEntity substitutionReasonCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBSTITUTION_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_SUBSTITUTION_PRACTITIONER"))
    PractitionerEntity substitutionPractitioner;

    @Column(name="noteDone")
    Boolean notDone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOT_DONE_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_NOT_DONE_CONCEPT"))
    ConceptEntity notDoneCode;




    public Set<MedicationDispenseIdentifier> getIdentifiers() {
        return identifiers;
    }

    public MedicationDispenseEntity setIdentifiers(Set<MedicationDispenseIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public Long getId() {
        return id;
    }

    public MedicationDispenseEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }


    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public MedicationDispenseEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }



    public EpisodeOfCareEntity getContextEpisodeOfCare() {
        return contextEpisodeOfCare;
    }

    public MedicationDispenseEntity setContextEpisodeOfCare(EpisodeOfCareEntity contextEpisodeOfCare) {
        this.contextEpisodeOfCare = contextEpisodeOfCare;
        return this;
    }

    public MedicationDispense.MedicationDispenseStatus getStatus() {
        return status;
    }

    public MedicationDispenseEntity setStatus(MedicationDispense.MedicationDispenseStatus status) {
        this.status = status;
        return this;
    }

    public ConceptEntity getCategoryCode() {
        return categoryCode;
    }

    public MedicationDispenseEntity setCategoryCode(ConceptEntity categoryCode) {
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

    public ConceptEntity getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(ConceptEntity typeCode) {
        this.typeCode = typeCode;
    }

    public BigDecimal getQuantityValue() {
        return quantityValue;
    }

    public void setQuantityValue(BigDecimal quantityValue) {
        this.quantityValue = quantityValue;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public String getDaysSupplyUnit() {
        return daysSupplyUnit;
    }

    public void setDaysSupplyUnit(String daysSupplyUnit) {
        this.daysSupplyUnit = daysSupplyUnit;
    }

    public BigDecimal getDaysSupplyValue() {
        return daysSupplyValue;
    }

    public void setDaysSupplyValue(BigDecimal daysSupplyValue) {
        this.daysSupplyValue = daysSupplyValue;
    }


    public Date getWhenPrepared() {
        return whenPrepared;
    }

    public void setWhenPrepared(Date whenPrepared) {
        this.whenPrepared = whenPrepared;
    }

    public Date getWhenHandedOver() {
        return whenHandedOver;
    }

    public void setWhenHandedOver(Date whenHandedOver) {
        this.whenHandedOver = whenHandedOver;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public PractitionerEntity getReceiverPractitioner() {
        return receiverPractitioner;
    }

    public void setReceiverPractitioner(PractitionerEntity receiverPractitioner) {
        this.receiverPractitioner = receiverPractitioner;
    }

    public OrganisationEntity getReceiverOrganisaton() {
        return receiverOrganisaton;
    }

    public void setReceiverOrganisaton(OrganisationEntity receiverOrganisaton) {
        this.receiverOrganisaton = receiverOrganisaton;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getSubstituted() {
        return substituted;
    }

    public void setSubstituted(Boolean substituted) {
        this.substituted = substituted;
    }

    public Set<MedicationDispenseDosage> getDosageInstructions() {
        return dosageInstructions;
    }

    public void setDosageInstructions(Set<MedicationDispenseDosage> dosageInstructions) {
        this.dosageInstructions = dosageInstructions;
    }

    public ConceptEntity getSubstitutionTypeCode() {
        return substitutionTypeCode;
    }

    public void setSubstitutionTypeCode(ConceptEntity substitutionTypeCode) {
        this.substitutionTypeCode = substitutionTypeCode;
    }

    public ConceptEntity getSubstitutionReasonCode() {
        return substitutionReasonCode;
    }

    public void setSubstitutionReasonCode(ConceptEntity substitutionReasonCode) {
        this.substitutionReasonCode = substitutionReasonCode;
    }

    public PractitionerEntity getSubstitutionPractitioner() {
        return substitutionPractitioner;
    }

    public void setSubstitutionPractitioner(PractitionerEntity substitutionPractitioner) {
        this.substitutionPractitioner = substitutionPractitioner;
    }

    public Boolean getNotDone() {
        return notDone;
    }

    public void setNotDone(Boolean notDone) {
        this.notDone = notDone;
    }

    public ConceptEntity getNotDoneCode() {
        return notDoneCode;
    }

    public void setNotDoneCode(ConceptEntity notDoneCode) {
        this.notDoneCode = notDoneCode;
    }
}
