package uk.nhs.careconnect.ri.database.entity.appointment;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name = "AppointmentReason",uniqueConstraints = @UniqueConstraint(name="PK_APPOINTMENT_REASON", columnNames={"APPOINTMENT_REASON_ID"})
        ,indexes = { @Index(name="IDX_APPOINTMENT_REASON", columnList = "REASON_CONCEPT_ID")}
)
public class AppointmentReason extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="APPOINTMENT_REASON_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "APPOINTMENT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_REASON_APPOINTMENT_ID"))
    private AppointmentEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REASON_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_REASON_REASON_CONCEPT_ID"))
    private ConceptEntity reason;

    @Override
    public Long getId() { return Id; }

    public void setId(Long id) { Id = id; }

    public AppointmentEntity getAppointment() { return appointment; }

    public void setAppointment(AppointmentEntity appointment) { this.appointment = appointment; }

    public ConceptEntity getReason() { return reason; }

    public void setReason(ConceptEntity reason) { this.reason = reason; }
}
