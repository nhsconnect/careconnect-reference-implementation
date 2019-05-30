package uk.nhs.careconnect.ri.database.entity.task;

import org.hl7.fhir.dstu3.model.Task;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
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

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Task.TaskStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_TASK_PATIENT_ID"))
    private PatientEntity patient;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_START")
    private Date periodStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_END")
    private Date periodEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENTERER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_ENTERED_PRACTITIONER_ID"))
    private PractitionerEntity entererPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENTERER_PATIENT",foreignKey= @ForeignKey(name="FK_ENTERED_PATIENT_ID"))
    private PatientEntity entererPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "INSURER_ORGANISATION",foreignKey= @ForeignKey(name="FK_INSURER_ORGANISATION_ID"))
    private OrganisationEntity insurerOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROVIDER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_PROVIDER_PRACTITIONER_ID"))
    private PractitionerEntity providerPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROVIDER_ORGANISATION",foreignKey= @ForeignKey(name="FK_PROVIDER_ORGANISATION_ID"))
    private OrganisationEntity providerOrganisation;


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


    @Override
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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



    public OrganisationEntity getInsurerOrganisation() {
        return insurerOrganisation;
    }

    public PractitionerEntity getEntererPractitioner() {
        return entererPractitioner;
    }

    public void setEntererPractitioner(PractitionerEntity entererPractitioner) {
        this.entererPractitioner = entererPractitioner;
    }

    public PatientEntity getEntererPatient() {
        return entererPatient;
    }

    public void setEntererPatient(PatientEntity entererPatient) {
        this.entererPatient = entererPatient;
    }

    public void setInsurerOrganisation(OrganisationEntity insurerOrganisation) {
        this.insurerOrganisation = insurerOrganisation;
    }

    public PractitionerEntity getProviderPractitioner() {
        return providerPractitioner;
    }

    public void setProviderPractitioner(PractitionerEntity providerPractitioner) {
        this.providerPractitioner = providerPractitioner;
    }

    public OrganisationEntity getProviderOrganisation() {
        return providerOrganisation;
    }

    public void setProviderOrganisation(OrganisationEntity providerOrganisation) {
        this.providerOrganisation = providerOrganisation;
    }


}
