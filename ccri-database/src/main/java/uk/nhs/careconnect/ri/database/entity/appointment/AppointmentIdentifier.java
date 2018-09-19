package uk.nhs.careconnect.ri.database.entity.appointment;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;

import javax.persistence.*;


@Entity
@Table(name="AppointmentIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_APPOINTMENT_IDENTIFIER", columnNames={"APPOINTMENT_IDENTIFIER_ID"})
		)
public class AppointmentIdentifier extends BaseIdentifier {

	public AppointmentIdentifier() {

	}

	public AppointmentIdentifier(AppointmentEntity appointment) {
		this.appointment = appointment;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "APPOINTMENT_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "APPOINTMENT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_APPOINTMENT_IDENTIFIER"))
	private AppointmentEntity appointment;

	public Long getIdentifierId() {
		return identifierId;
	}

	public void setIdentifierId(Long identifierId) {
		this.identifierId = identifierId;
	}

	public AppointmentEntity getAppointment() {
		return appointment;
	}

	public void setAppointment(AppointmentEntity appointment) {
		this.appointment = appointment;
	}
}
