package uk.nhs.careconnect.ri.database.entity.slot;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.Slot;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;
import javax.persistence.*;
import java.util.*;

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
    private String slotName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SERVICE_CATEGORY",nullable = false)
    private ConceptEntity serviceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="APPOINTMENT_TYPE",nullable = false)
    private ConceptEntity appointmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SCHEDULE",nullable = false)
    private ScheduleEntity schedule;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SLOT_START")
    private Date slotStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SLOT_END")
    private Date slotEnd;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "SLOT_STATUS")
    private Slot.SlotStatus slotStatus;

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

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

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

    public Date getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(Date slotStart) {
        this.slotStart = slotStart;
    }

    public Date getSlotEnd() {
        return slotEnd;
    }

    public void setSlotEnd(Date slotEnd) {
        this.slotEnd = slotEnd;
    }

    public Slot.SlotStatus getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(Slot.SlotStatus slotStatus) {
        this.slotStatus = slotStatus;
    }

    public Set<SlotIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<SlotIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
}
