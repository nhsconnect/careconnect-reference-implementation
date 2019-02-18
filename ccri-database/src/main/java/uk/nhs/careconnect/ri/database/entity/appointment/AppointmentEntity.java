package uk.nhs.careconnect.ri.database.entity.appointment;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import org.hl7.fhir.dstu3.model.Appointment.*;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.*;
import org.hl7.fhir.dstu3.model.*;

@Entity
@Table(name = "Appointment")
public class AppointmentEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPOINTMENT_ID")
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "APPOINTMENT_STATUS")
    private Appointment.AppointmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_CONCEPT_ID", foreignKey = @ForeignKey(name = "FK_APPOINTMENT_TYPE_CONCEPT_ID"))
    private ConceptEntity apointmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REASON_CONCEPT_ID", foreignKey = @ForeignKey(name = "FK_APPOINTMENT_REASON_CONCEPT_ID"))
    private ConceptEntity reason;

    @Column(name = "PRIORITY")
    private int priority;

    @Column(name = "DESCRIPTION")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "APPOINTMENT_START")
    private Date start;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "APPOINTMENT_END")
    private Date end;

    //@ManyToOne(fetch = FetchType.LAZY)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPOINTMENT_SLOT_ID", foreignKey = @ForeignKey(name = "FK_APPOINTMENT_SLOT"))
    private SlotEntity slot;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "APPOINTMENT_CREATED")
    private Date created;

    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "PARTICIPANT")
    private BackboneElement participant;

    @OneToMany(mappedBy="appointment", targetEntity = AppointmentIdentifier.class)
    Set<AppointmentIdentifier> identifiers = new HashSet<>();

    public Set<AppointmentIdentifier> getIdentifiers() { return identifiers; }

    public void setIdentifiers(Set<AppointmentIdentifier> identifiers) { this.identifiers = identifiers; }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public ConceptEntity getApointmentType() {
        return apointmentType;
    }

    public void setApointmentType(ConceptEntity apointmentType) {
        this.apointmentType = apointmentType;
    }

    public ConceptEntity getReason() {
        return reason;
    }

    public void setReason(ConceptEntity reason) {
        this.reason = reason;
    }

    @OneToMany(mappedBy="appointment", targetEntity= AppointmentReason.class)
    private Set<AppointmentReason> reasons = new HashSet<>();

    public Set<AppointmentReason> getReasons() {
        return reasons;
    }

    public void setReasons(Set<AppointmentReason> reasons) {
        this.reasons = reasons;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public SlotEntity getSlot() {
        return slot;
    }

    public void setSlot(SlotEntity slot) {
        this.slot = slot;
    }

    @Override
    public Date getCreated() { return created; }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BackboneElement getParticipant() {
        return participant;
    }

    public void setParticipant(BackboneElement participant) {
        this.participant = participant;
    }
}
