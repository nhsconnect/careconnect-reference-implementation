package uk.nhs.careconnect.ri.database.entity.appointment;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;

import javax.persistence.*;

@Entity
@Table(name = "AppointmentSlot")
public class AppointmentSlot extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="APPOINTMENT_SLOT_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPOINTMENT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_SLOT_SERVICE_ID"))
    private AppointmentEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SLOT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_SLOT_SLOT_ID"))
    private SlotEntity slot;

    @Override
    public Long getId() { return Id; }

    public void setId(Long id) { Id = id; }

    public AppointmentEntity getService() { return service; }

    public void setService(AppointmentEntity service) { this.service = service; }

    public SlotEntity getSlot() { return slot; }

    public void setSlot(SlotEntity slot) { this.slot = slot; }
}
