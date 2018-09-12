package uk.nhs.careconnect.ri.database.entity.clinicialImpression;

import org.hl7.fhir.dstu3.model.ClinicalImpression;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "ClinicalImpression",
        indexes = {

        })
public class ClinicalImpressionEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum ClinicalImpressionType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="IMPRESSION_ID")
    private Long id;

    @OneToMany(mappedBy="impression", targetEntity=ClinicalImpressionIdentifier.class)
    private Set<ClinicalImpressionIdentifier> identifiers = new HashSet<>();
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private ClinicalImpression.ClinicalImpressionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_IMPRESSION_CODE_CONCEPT_ID"))
    private ConceptEntity impressionCode;

    @Column(name="DESCRIPTION")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_PATIENT_ID"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EFFECTIVE_START_DATETIME")
    private Date effectiveStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EFFECTIVE_END_DATETIME")
    private Date effectiveEndDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "IMPRESSION_END_DATETIME")
    private Date impressionDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ASSESSOR_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_ASSESSOR_PRACTITIONERT_ID"))
    private PractitionerEntity assessorPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROBLEM_CONDITION_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_PROBLEM_CONDITION_ID"))
    private ConditionEntity problemCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROBLEM_ALLERGY_ID",foreignKey= @ForeignKey(name="FK_IMPRESSION_PROBLEM_ALLERGY_ID"))
    private AllergyIntoleranceEntity problemAllergy;

    @Column(name="SUMMARY")
    private String summary;

    @OneToMany(mappedBy="impression", targetEntity=ClinicalImpressionFinding.class)
    private Set<ClinicalImpressionFinding> findings = new HashSet<>();

    @OneToMany(mappedBy="impression", targetEntity=ClinicalImpressionPrognosis.class)
    private Set<ClinicalImpressionPrognosis> prognosis = new HashSet<>();

    @Column(name="NOTE")
    private String note;

    public Long getId() {
        return id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClinicalImpression.ClinicalImpressionStatus getStatus() {
        return status;
    }


    public ClinicalImpressionEntity setStatus(ClinicalImpression.ClinicalImpressionStatus status) {
        this.status = status;
        return this;
    }

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public ConceptEntity getRiskCode() {
        return impressionCode;
    }

    public void setRiskCode(ConceptEntity impressionCode) {
        this.impressionCode = impressionCode;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }

    public Set<ClinicalImpressionIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<ClinicalImpressionIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public ConceptEntity getImpressionCode() {
        return impressionCode;
    }

    public void setImpressionCode(ConceptEntity impressionCode) {
        this.impressionCode = impressionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEffectiveStartDateTime() {
        return effectiveStartDateTime;
    }

    public void setEffectiveStartDateTime(Date effectiveStartDateTime) {
        this.effectiveStartDateTime = effectiveStartDateTime;
    }

    public Date getEffectiveEndDateTime() {
        return effectiveEndDateTime;
    }

    public void setEffectiveEndDateTime(Date effectiveEndDateTime) {
        this.effectiveEndDateTime = effectiveEndDateTime;
    }

    public Date getImpressionDateTime() {
        return impressionDateTime;
    }

    public void setImpressionDateTime(Date impressionDateTime) {
        this.impressionDateTime = impressionDateTime;
    }

    public PractitionerEntity getAssessorPractitioner() {
        return assessorPractitioner;
    }

    public void setAssessorPractitioner(PractitionerEntity assessorPractitioner) {
        this.assessorPractitioner = assessorPractitioner;
    }

    public ConditionEntity getProblemCondition() {
        return problemCondition;
    }

    public void setProblemCondition(ConditionEntity problemCondition) {
        this.problemCondition = problemCondition;
    }

    public AllergyIntoleranceEntity getProblemAllergy() {
        return problemAllergy;
    }

    public void setProblemAllergy(AllergyIntoleranceEntity problemAllergy) {
        this.problemAllergy = problemAllergy;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Set<ClinicalImpressionFinding> getFindings() {
        return findings;
    }

    public void setFindings(Set<ClinicalImpressionFinding> findings) {
        this.findings = findings;
    }

    public Set<ClinicalImpressionPrognosis> getPrognosis() {
        return prognosis;
    }

    public void setPrognosis(Set<ClinicalImpressionPrognosis> prognosis) {
        this.prognosis = prognosis;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
