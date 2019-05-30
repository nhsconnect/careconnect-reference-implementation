package uk.nhs.careconnect.ri.database.entity.task;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import javax.persistence.*;

@Entity
@Table(name="TaskIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_TASK_IDENTIFIER", columnNames={"TASK_IDENTIFIER_ID"})
		,indexes = {}
		)
public class TaskIdentifier extends BaseIdentifier {

	public TaskIdentifier() {
	}
    public TaskIdentifier(TaskEntity task) {
		this.task = task;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "TASK_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "TASK_ID",foreignKey= @ForeignKey(name="FK_TASK_IDENTIFIER_TASK_ID"))

    private TaskEntity task;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public TaskEntity getTask() {
		return task;
	}

	public void setTask(TaskEntity task) {
		this.task = task;
	}


}
