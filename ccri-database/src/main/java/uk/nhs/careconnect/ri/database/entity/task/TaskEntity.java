package uk.nhs.careconnect.ri.database.entity.task;

import org.hl7.fhir.dstu3.model.Task;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Person.PersonEntity;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "Task",
        indexes = {

        })
public class TaskEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="TASK_ID")
    private Long id;

    @OneToMany(mappedBy="task", targetEntity=TaskIdentifier.class)
    private Set<TaskIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="task", targetEntity=TaskPartOf.class)
    private Set<TaskPartOf> partOfs = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Task.TaskStatus status;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="intent")
    private Task.TaskIntent intent;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="priority")
    private Task.TaskPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FOCUS_CLAIM_ID",foreignKey= @ForeignKey(name="FK_TASK_FOCUS_CLAIM_ID"))
    private ClaimEntity focusClaim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FOR_PATIENT_ID",foreignKey= @ForeignKey(name="FK_TASK_PATIENT_ID"))
    private PatientEntity forPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CONTEXT_ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_TASK_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "AUTHORED")
    private Date authored;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_START")
    private Date periodStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_END")
    private Date periodEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REQUESTER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_REQUESTER_PRACTITIONER_ID"))
    private PractitionerEntity requesterPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REQUESTER_PATIENT",foreignKey= @ForeignKey(name="FK_REQUESTER_PATIENT_ID"))
    private PatientEntity requesterPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REQUESTER_ORGANISATION",foreignKey= @ForeignKey(name="FK_REQUESTER_ORGANISATION_ID"))
    private OrganisationEntity requesterOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OWNER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_OWNER_PRACTITIONER_ID"))
    private PractitionerEntity ownerPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OWNER_ORGANISATION",foreignKey= @ForeignKey(name="FK_OWNER_ORGANISATION_ID"))
    private OrganisationEntity ownerOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OWNER_PATIENT",foreignKey= @ForeignKey(name="FK_OWNER_PATIENT_ID"))
    private PatientEntity ownerPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OWNER_RELATED_PERSON",foreignKey= @ForeignKey(name="FK_OWNER_PERSON_ID"))
    private PersonEntity ownerPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OWNER_CARE_TEAM",foreignKey= @ForeignKey(name="FK_OWNER_CARE_TEAM_ID"))
    private CareTeamEntity ownerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OWNER_SERVICE",foreignKey= @ForeignKey(name="FK_OWNER_SERVICE_ID"))
    private HealthcareServiceEntity ownerService;

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }



    public Set<TaskIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<TaskIdentifier>(); }
        return identifiers;
    }


    public TaskEntity setIdentifiers(Set<TaskIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public Task.TaskStatus getStatus() {
        return status;
    }

    public void setStatus(Task.TaskStatus status) {
        this.status = status;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Set<TaskPartOf> getPartOfs() {
        return partOfs;
    }

    public void setPartOfs(Set<TaskPartOf> partOfs) {
        this.partOfs = partOfs;
    }

    public Task.TaskIntent getIntent() {
        return intent;
    }

    public void setIntent(Task.TaskIntent intent) {
        this.intent = intent;
    }

    public Task.TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(Task.TaskPriority priority) {
        this.priority = priority;
    }

    public PatientEntity getForPatient() {
        return forPatient;
    }

    public void setForPatient(PatientEntity forPatient) {
        this.forPatient = forPatient;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }

    public Date getAuthored() {
        return authored;
    }

    public void setAuthored(Date authored) {
        this.authored = authored;
    }


    public PractitionerEntity getOwnerPractitioner() {
        return ownerPractitioner;
    }

    public void setOwnerPractitioner(PractitionerEntity ownerPractitioner) {
        this.ownerPractitioner = ownerPractitioner;
    }

    public OrganisationEntity getOwnerOrganisation() {
        return ownerOrganisation;
    }

    public void setOwnerOrganisation(OrganisationEntity ownerOrganisation) {
        this.ownerOrganisation = ownerOrganisation;
    }

    public PatientEntity getOwnerPatient() {
        return ownerPatient;
    }

    public void setOwnerPatient(PatientEntity ownerPatient) {
        this.ownerPatient = ownerPatient;
    }

    public PersonEntity getOwnerPerson() {
        return ownerPerson;
    }

    public void setOwnerPerson(PersonEntity ownerPerson) {
        this.ownerPerson = ownerPerson;
    }

    public CareTeamEntity getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(CareTeamEntity ownerTeam) {
        this.ownerTeam = ownerTeam;
    }

    public HealthcareServiceEntity getOwnerService() {
        return ownerService;
    }

    public void setOwnerService(HealthcareServiceEntity ownerService) {
        this.ownerService = ownerService;
    }

    public PractitionerEntity getRequesterPractitioner() {
        return requesterPractitioner;
    }

    public void setRequesterPractitioner(PractitionerEntity requesterPractitioner) {
        this.requesterPractitioner = requesterPractitioner;
    }

    public PatientEntity getRequesterPatient() {
        return requesterPatient;
    }

    public void setRequesterPatient(PatientEntity requesterPatient) {
        this.requesterPatient = requesterPatient;
    }

    public OrganisationEntity getRequesterOrganisation() {
        return requesterOrganisation;
    }

    public void setRequesterOrganisation(OrganisationEntity requesterOrganisation) {
        this.requesterOrganisation = requesterOrganisation;
    }

    public ClaimEntity getFocusClaim() {
        return focusClaim;
    }

    public void setFocusClaim(ClaimEntity focusClaim) {
        this.focusClaim = focusClaim;
    }
}
