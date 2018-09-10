package uk.nhs.careconnect.ri.entity.schedule;

import uk.nhs.careconnect.ri.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;

import javax.persistence.*;


@Entity
@Table(name="ScheduleIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_SCHEDULE_IDENTIFIER", columnNames={"SCHEDULE_IDENTIFIER_ID"})
		)
public class ScheduleIdentifier extends BaseIdentifier {

	public ScheduleIdentifier() {

	}

	public ScheduleIdentifier(ScheduleEntity schedule) {
		this.schedule = schedule;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "SCHEDULE_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "SCHEDULE_ID",foreignKey= @ForeignKey(name="FK_SCHEDULE_SCHEDULE_IDENTIFIER"))
	private ScheduleEntity schedule;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }


	public ScheduleEntity getService() {
		return schedule;
	}

	public void setService(ScheduleEntity schedule) {
		this.schedule = schedule;
	}
}
