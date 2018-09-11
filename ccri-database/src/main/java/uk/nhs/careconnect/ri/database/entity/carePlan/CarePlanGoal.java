package uk.nhs.careconnect.ri.database.entity.carePlan;

import uk.nhs.careconnect.ri.database.entity.goal.GoalEntity;

import javax.persistence.*;

@Entity
@Table(name="CarePlanGoal", uniqueConstraints= @UniqueConstraint(name="PK_CAREPLAN_GOAL", columnNames={"CAREPLAN_GOAL_ID"})
		,indexes = {}
		)
public class CarePlanGoal {

	public CarePlanGoal() {
	}
    public CarePlanGoal(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CAREPLAN_GOAL_ID")
    private Long carePlanGoal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CAREPLAN_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_GOAL_CAREPLAN_ID"))
    private CarePlanEntity carePlan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="GOAL_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_GOAL_GOAL_ID"))
	private GoalEntity goal;

	public CarePlanEntity getCarePlan() {
		return carePlan;
	}

	public void setCarePlan(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}

	public Long getCarePlanGoal() {
		return carePlanGoal;
	}

	public void setCarePlanGoal(Long carePlanGoal) {
		this.carePlanGoal = carePlanGoal;
	}

	public GoalEntity getGoal() {
		return goal;
	}

	public void setGoal(GoalEntity goal) {
		this.goal = goal;
	}
}
