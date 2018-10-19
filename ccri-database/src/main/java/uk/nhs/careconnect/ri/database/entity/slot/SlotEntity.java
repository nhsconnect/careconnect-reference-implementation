package uk.nhs.careconnect.ri.database.entity.slot;

import org.hl7.fhir.dstu3.model.Slot;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Slot")
public class SlotEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SLOT_ID")
    private Long id;

    @Column(name="ACTIVE")
    private Boolean active;

    @Column(name="SLOT_NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SERVICE_CATEGORY")
    private ConceptEntity serviceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="APPOINTMENT_TYPE")
    private ConceptEntity appointmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SCHEDULE")
    private ScheduleEntity schedule;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SLOT_START")
    private Date Start;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SLOT_END")
    private Date End;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "SLOT_STATUS")
    private Slot.SlotStatus Status;

    @OneToMany(mappedBy="slot", targetEntity = SlotIdentifier.class)
    Set<SlotIdentifier> identifiers = new HashSet<>();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public ConceptEntity getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ConceptEntity serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public ConceptEntity getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(ConceptEntity appointmentType) {
        this.appointmentType = appointmentType;
    }

    public ScheduleEntity getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleEntity schedule) {
        this.schedule = schedule;
    }

    public Date getStart() { return Start; }

    public void setStart(Date start) { Start = start; }

    public Date getEnd() { return End; }

    public void setEnd(Date end) { End = end; }

    public Slot.SlotStatus getStatus() { return Status; }

    public void setStatus(Slot.SlotStatus status) { Status = status; }

    public Set<SlotIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<SlotIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
}
