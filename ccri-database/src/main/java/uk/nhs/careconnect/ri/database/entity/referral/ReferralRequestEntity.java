package uk.nhs.careconnect.ri.database.entity.referral;

import org.hl7.fhir.dstu3.model.ReferralRequest;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ReferralRequest",
        indexes = {
                @Index(name = "IDX_REFERRAL_DATE", columnList="authoredOn"),
        })
public class ReferralRequestEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="REFERRAL_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_REFERRAL"))

    private PatientEntity patient;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="STATUS")
    private ReferralRequest.ReferralRequestStatus status;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="INTENT")
    private ReferralRequest.ReferralCategory intent;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="PRIORITY")
    private ReferralRequest.ReferralPriority priority;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "authoredOn")
    private Date authoredOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CONTEXT_ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_ENCOUNTER"))
    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SPECIALTY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_SPECIALTY_CONCEPT"))
    private ConceptEntity specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_TYPE_CONCEPT"))
    private ConceptEntity type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REQUESTOR_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_ORGANISATION"))

    private OrganisationEntity requesterOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ONBEHALF_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_ONBEHALF_ORGANISATION"))

    private OrganisationEntity onBehalfOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REQUESTOR_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_PRACTITIONER"))

    private PractitionerEntity requesterPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REQUESTOR_PATIENT_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_PATIENT"))

    private PatientEntity requesterPatient;

    @OneToMany(mappedBy="referral", targetEntity=ReferralRequestIdentifier.class)

    private Set<ReferralRequestIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="referral", targetEntity=ReferralRequestRecipient.class)

    private Set<ReferralRequestRecipient> recipients = new HashSet<>();

    @OneToMany(mappedBy="referral", targetEntity=ReferralRequestReason.class)

    private Set<ReferralRequestReason> reasons = new HashSet<>();

    @OneToMany(mappedBy="referral", targetEntity=ReferralRequestServiceRequested.class)

    private Set<ReferralRequestServiceRequested> services = new HashSet<>();

    public void setId(Long id) {
        this.id = id;
    }

    public ReferralRequest.ReferralRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ReferralRequest.ReferralRequestStatus status) {
        this.status = status;
    }

    public Date getAuthoredOn() {
        return authoredOn;
    }

    public void setAuthoredOn(Date authoredOn) {
        this.authoredOn = authoredOn;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }

    public Set<ReferralRequestReason> getReasons() {
        return reasons;
    }

    public void setReasons(Set<ReferralRequestReason> reasons) {
        this.reasons = reasons;
    }

    public Set<ReferralRequestServiceRequested> getServices() {
        return services;
    }

    public void setServices(Set<ReferralRequestServiceRequested> services) {
        this.services = services;
    }

    public ReferralRequest.ReferralCategory getIntent() {
        return intent;
    }

    public void setIntent(ReferralRequest.ReferralCategory intent) {
        this.intent = intent;
    }

    public ReferralRequest.ReferralPriority getPriority() {
        return priority;
    }

    public void setPriority(ReferralRequest.ReferralPriority priority) {
        this.priority = priority;
    }

    public Set<ReferralRequestIdentifier> getIdentifiers() {
        return identifiers;
    }

    public ReferralRequestEntity setIdentifiers(Set<ReferralRequestIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public Set<ReferralRequestRecipient> getRecipients() {
        return recipients;
    }

    public ReferralRequestEntity setRecipients(Set<ReferralRequestRecipient> recipients) {
        this.recipients = recipients;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ReferralRequestEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public ConceptEntity getType() {
        return type;
    }

    public ReferralRequestEntity setType(ConceptEntity type) {
        this.type = type;
        return this;
    }

    public ConceptEntity getSpecialty() {
        return specialty;
    }

    public ReferralRequestEntity setSpecialty(ConceptEntity specialty) {
        this.specialty = specialty;
        return this;
    }

    public OrganisationEntity getRequesterOrganisation() {
        return requesterOrganisation;
    }

    public ReferralRequestEntity setRequesterOrganisation(OrganisationEntity requesterOrganisation) {
        this.requesterOrganisation = requesterOrganisation;
        return this;
    }

    public PractitionerEntity getRequesterPractitioner() {
        return requesterPractitioner;
    }

    public ReferralRequestEntity setRequesterPractitioner(PractitionerEntity requesterPractitioner) {
        this.requesterPractitioner = requesterPractitioner;
        return this;
    }

    public PatientEntity getRequesterPatient() {
        return requesterPatient;
    }

    public void setRequesterPatient(PatientEntity requesterPatient) {
        this.requesterPatient = requesterPatient;
    }

    public OrganisationEntity getOnBehalfOrganisation() {
        return onBehalfOrganisation;
    }

    public void setOnBehalfOrganisation(OrganisationEntity onBehalfOrganisation) {
        this.onBehalfOrganisation = onBehalfOrganisation;
    }
}
