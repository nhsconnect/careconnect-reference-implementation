package uk.nhs.careconnect.ri.database.entity.goal;

import org.hl7.fhir.dstu3.model.Goal;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Goal",
        indexes = {

        })
public class GoalEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum GoalType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="GOAL_ID")
    private Long id;

    @OneToMany(mappedBy="goal", targetEntity=GoalIdentifier.class)
    private Set<GoalIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Goal.GoalStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CATEGORY_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_GOAL_CATEGORY_CONCEPT_ID"))
    private ConceptEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PRIORITY_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_GOAL_PRIORITY_CONCEPT_ID"))
    private ConceptEntity priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="DESCRIPTION_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_GOAL_DESCRIPTION_CONCEPT_ID"))
    private ConceptEntity description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_GOAL_PATIENT_ID"))
    private PatientEntity patient;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_DATETIME")
    private Date startDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="START_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_GOAL_START_CONCEPT_ID"))
    private ConceptEntity startConcept;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "STATUS_DateTime")
    private Date statusDateTime;

    @Column(name="STATUS_REASON",length = MAX_DESC_LENGTH,nullable = true)
    private String statusReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "EXPRESSEDBY_PATIENT_ID",foreignKey= @ForeignKey(name="FK_GOAL_EXPRESSEDBY_PATIENT_ID"))
    private PatientEntity expressedByPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "EXPRESSEDBY_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_GOAL_EXPRESSEDBY_PRACTITIONER_ID"))
    private PractitionerEntity expressedByPractitioner;

    @OneToMany(mappedBy="goal", targetEntity=GoalAddresses.class)
    private Set<GoalAddresses> addresses = new HashSet<>();

    @Column(name="NOTE",length = MAX_DESC_LENGTH,nullable = true)
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



    public Set<GoalIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<GoalIdentifier>(); }
        return identifiers;
    }

    public Goal.GoalStatus getStatus() {
        return status;
    }

    public GoalEntity setIdentifiers(Set<GoalIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


    public GoalEntity setStatus(Goal.GoalStatus status) {
        this.status = status;
        return this;
    }

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public ConceptEntity getCategory() {
        return category;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }

    public ConceptEntity getPriority() {
        return priority;
    }

    public void setPriority(ConceptEntity priority) {
        this.priority = priority;
    }

    public ConceptEntity getDescription() {
        return description;
    }

    public void setDescription(ConceptEntity description) {
        this.description = description;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public ConceptEntity getStartConcept() {
        return startConcept;
    }

    public void setStartConcept(ConceptEntity startConcept) {
        this.startConcept = startConcept;
    }

    public Date getStatusDateTime() {
        return statusDateTime;
    }

    public void setStatusDateTime(Date statusDateTime) {
        this.statusDateTime = statusDateTime;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public PatientEntity getExpressedByPatient() {
        return expressedByPatient;
    }

    public void setExpressedByPatient(PatientEntity expressedByPatient) {
        this.expressedByPatient = expressedByPatient;
    }

    public PractitionerEntity getExpressedByPractitioner() {
        return expressedByPractitioner;
    }

    public void setExpressedByPractitioner(PractitionerEntity expressedByPractitioner) {
        this.expressedByPractitioner = expressedByPractitioner;
    }

    public Set<GoalAddresses> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<GoalAddresses> addresses) {
        this.addresses = addresses;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
