package uk.nhs.careconnect.ri.database.entity.documentReference;

import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Enumerations;
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
@Table(name = "DocumentReference", indexes = {



})
public class DocumentReferenceEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="DOCUMENT_REFERENCE_ID")
    private Long id;



    @OneToMany(mappedBy="documentReference", targetEntity=DocumentReferenceIdentifier.class)
    private Set<DocumentReferenceIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    private Enumerations.DocumentReferenceStatus status;

    @Enumerated(EnumType.ORDINAL)
    private DocumentReference.ReferredDocumentStatus docStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_TYPE_CONCEPT_ID"))

    private ConceptEntity type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CLASS_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_CLASS_CONCEPT_ID"))

    private ConceptEntity class_;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created")
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "indexed")
    private Date indexed;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_PATIENT_ID"))
    private PatientEntity patient;
    // subject

    @OneToMany(mappedBy="documentReference", targetEntity=DocumentReferenceAuthor.class)
    private Set<DocumentReferenceAuthor> authors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="AUTHENTICATOR_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_AUTHENTICATOR_PRACTITIONER"))

    private PractitionerEntity authenticatorPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="AUTHENTICATOR_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_AUTHENTICATOR_ORGANISATION"))

    private OrganisationEntity authenticatorOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CUSTODIAN_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_CUSTODIAN_ORGANISATION"))

    private OrganisationEntity custodian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FORMAT_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_FORMAT_CONCEPT_ID"))
    private ConceptEntity contentFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CONTEXT_ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PRACTICE_SETTING_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_PRACTICE_SETTING_CONCEPT_ID"))
    private ConceptEntity contextPracticeSetting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FACILITY_TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_FACILITY_TYPE_CONCEPT_ID"))
    private ConceptEntity contextFaciltityType;

    @OneToMany(mappedBy="documentReference", targetEntity=DocumentReferenceAttachment.class)
    private Set<DocumentReferenceAttachment> attachments = new HashSet<>();

    public Set<DocumentReferenceIdentifier> getIdentifiers() {
        return identifiers;
    }

    public Enumerations.DocumentReferenceStatus getStatus() {
        return status;
    }

    public void setStatus(Enumerations.DocumentReferenceStatus status) {
        this.status = status;
    }

    public DocumentReference.ReferredDocumentStatus getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(DocumentReference.ReferredDocumentStatus docStatus) {
        this.docStatus = docStatus;
    }

    public ConceptEntity getType() {
        return type;
    }

    public void setType(ConceptEntity type) {
        this.type = type;
    }

    public ConceptEntity getClass_() {
        return class_;
    }

    public void setClass_(ConceptEntity class_) {
        this.class_ = class_;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getIndexed() {
        return indexed;
    }

    public void setIndexed(Date indexed) {
        this.indexed = indexed;
    }

    public Set<DocumentReferenceAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<DocumentReferenceAuthor> authors) {
        this.authors = authors;
    }

    public PractitionerEntity getAuthenticatorPractitioner() {
        return authenticatorPractitioner;
    }

    public void setAuthenticatorPractitioner(PractitionerEntity authenticatorPractitioner) {
        this.authenticatorPractitioner = authenticatorPractitioner;
    }

    public OrganisationEntity getAuthenticatorOrganisation() {
        return authenticatorOrganisation;
    }

    public void setAuthenticatorOrganisation(OrganisationEntity authenticatorOrganisation) {
        this.authenticatorOrganisation = authenticatorOrganisation;
    }

    public OrganisationEntity getCustodian() {
        return custodian;
    }

    public void setCustodian(OrganisationEntity custodian) {
        this.custodian = custodian;
    }

    public ConceptEntity getContentFormat() {
        return contentFormat;
    }

    public void setContentFormat(ConceptEntity contentFormat) {
        this.contentFormat = contentFormat;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }

    public ConceptEntity getContextPracticeSetting() {
        return contextPracticeSetting;
    }

    public void setContextPracticeSetting(ConceptEntity contextPracticeSetting) {
        this.contextPracticeSetting = contextPracticeSetting;
    }

    public ConceptEntity getContextFaciltityType() {
        return contextFaciltityType;
    }

    public void setContextFaciltityType(ConceptEntity contextFaciltityType) {
        this.contextFaciltityType = contextFaciltityType;
    }

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

    public DocumentReferenceEntity setIdentifiers(Set<DocumentReferenceIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public Set<DocumentReferenceAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<DocumentReferenceAttachment> attachments) {
        this.attachments = attachments;
    }
}
