package uk.nhs.careconnect.ri.database.entity.diagnosticReport;


import org.hl7.fhir.dstu3.model.DiagnosticReport;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "DiagnosticReport")
public class DiagnosticReportEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 7000;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="DIAGNOSTIC_REPORT_ID")
    private Long id;


    @OneToMany(mappedBy="diagnosticReport", targetEntity = DiagnosticReportIdentifier.class)

    Set<DiagnosticReportIdentifier> identifiers = new HashSet<>();

    @Column(name="status")
    DiagnosticReport.DiagnosticReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CATEGORY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_CATEGORY"))

    private ConceptEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_CODE"))

    private ConceptEntity code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",nullable = false, foreignKey= @ForeignKey(name="FK_PATIENT_DIAGNOSTIC_REPORT"))

    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONTEXT_ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_CONTEXT_ENCOUNTER"))

    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONTEXT_EPISODE_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_CONTEXT_EPISODE"))

    private EncounterEntity contextEpisodeOfCare;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effectiveDateTime")
    private Date effectiveDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued")
    private Date issued;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PERFORMER_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_PERFORMER_PRACTITIONER"))

    private PractitionerEntity performerPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PERFORMER_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_DIAGNOSTIC_REPORT_PERFORMER_ORGANISATION"))

    private OrganisationEntity performerOrganisation;

    @Column(name="CONCLUSION_REPORT",length = MAX_DESC_LENGTH,nullable = true)
    private String conclusion;

    @OneToMany(mappedBy="diagnosticReport", targetEntity = DiagnosticReportResult.class)

    Set<DiagnosticReportResult> results = new HashSet<>();

    @OneToMany(mappedBy="diagnosticReport", targetEntity = DiagnosticReportDiagnosis.class)

    Set<DiagnosticReportDiagnosis> diagnosises = new HashSet<>();

    @Override
    public Long getId() {
        return this.id;
    }

    public DiagnosticReportEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public DiagnosticReportEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }


    public DiagnosticReport.DiagnosticReportStatus getStatus() {
        return status;
    }

    public DiagnosticReportEntity setStatus(DiagnosticReport.DiagnosticReportStatus status) {
        this.status = status;
        return this;
    }

    public Set<DiagnosticReportIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<DiagnosticReportIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public ConceptEntity getCategory() {
        return category;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public void setCode(ConceptEntity code) {
        this.code = code;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }

    public EncounterEntity getContextEpisodeOfCare() {
        return contextEpisodeOfCare;
    }

    public void setContextEpisodeOfCare(EncounterEntity contextEpisodeOfCare) {
        this.contextEpisodeOfCare = contextEpisodeOfCare;
    }

    public Date getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public void setEffectiveDateTime(Date effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
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

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public Set<DiagnosticReportResult> getResults() {
        return results;
    }

    public void setResults(Set<DiagnosticReportResult> results) {
        this.results = results;
    }

    public Set<DiagnosticReportDiagnosis> getDiagnosises() {
        return diagnosises;
    }

    public void setDiagnosises(Set<DiagnosticReportDiagnosis> diagnosises) {
        this.diagnosises = diagnosises;
    }
}
