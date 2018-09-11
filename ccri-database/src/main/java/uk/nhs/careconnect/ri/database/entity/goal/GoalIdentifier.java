package uk.nhs.careconnect.ri.database.entity.goal;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;


import javax.persistence.*;

@Entity
@Table(name="GoalIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_GOAL_IDENTIFIER", columnNames={"GOAL_IDENTIFIER_ID"})
		,indexes = {}
		)
public class GoalIdentifier extends BaseIdentifier {

	public GoalIdentifier() {
	}
    public GoalIdentifier(GoalEntity goal) {
		this.goal = goal;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "GOAL_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "GOAL_ID",foreignKey= @ForeignKey(name="FK_GOAL_IDENTIFIER_GOAL_ID"))

    private GoalEntity goal;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public GoalEntity getGoal() {
		return goal;
	}

	public void setGoal(GoalEntity goal) {
		this.goal = goal;
	}


}
