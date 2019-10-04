package uk.nhs.careconnect.ri.database.entity.task;

import uk.nhs.careconnect.ri.database.entity.BaseResource;


import javax.persistence.*;

@Entity
@Table(name="TaskPartOf", uniqueConstraints= @UniqueConstraint(name="PK_TASK_PARTOF", columnNames={"TASK_PARTOF_ID"})
		,indexes = {}
		)
public class TaskPartOf extends BaseResource {

	public TaskPartOf() {
	}
    public TaskPartOf(TaskEntity task) {
		this.task = task;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "TASK_PARTOF_ID")
    private Long partOfId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "TASK_ID",foreignKey= @ForeignKey(name="FK_TASK_PARTOF_TASK_ID"))
	private TaskEntity task;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "RELATED_TASK_ID",foreignKey= @ForeignKey(name="FK_RELATED_TASK_ID"))
	private TaskEntity partOfTask;

	public TaskEntity getTask() {
		return task;
	}

	public void setTask(TaskEntity task) {
		this.task = task;
	}

	public Long getRelatedId() {
		return partOfId;
	}

	public void setRelatedId(Long partOfId) {
		this.partOfId = partOfId;
	}

	public TaskEntity getRelatedTask() {
		return partOfTask;
	}

	public void setRelatedTask(TaskEntity partOfTask) {
		this.partOfTask = partOfTask;
	}

	@Override
	public Long getId() {
		return getRelatedId();
	}
}
