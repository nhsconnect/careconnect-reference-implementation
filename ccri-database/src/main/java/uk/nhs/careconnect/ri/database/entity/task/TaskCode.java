package uk.nhs.careconnect.ri.database.entity.task;

import uk.nhs.careconnect.ri.database.entity.BaseCodeableConcept;
import javax.persistence.*;

@Entity
@Table(name="TaskCode")
public class TaskCode extends BaseCodeableConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "TASK_CODE_ID")
    private Long Id;

    @OneToOne(mappedBy = "code")
    private TaskEntity task;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }
}
