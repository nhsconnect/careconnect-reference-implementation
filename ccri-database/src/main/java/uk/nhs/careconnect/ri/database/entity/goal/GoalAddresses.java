package uk.nhs.careconnect.ri.database.entity.goal;

import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;

@Entity
@Table(name="GoalAddresses", uniqueConstraints= @UniqueConstraint(name="PK_GOAL_ADDRESSES", columnNames={"GOAL_ADDRESSES_ID"})
		,indexes = {}
		)
public class GoalAddresses extends BaseResource {

	public GoalAddresses() 
	{
	}
    public GoalAddresses(GoalEntity goal) {
		this.goal = goal;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "GOAL_ADDRESSES_ID")
    private Long addressesId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "GOAL_ID")
	private GoalEntity goal;

	public Long getAddressesId() { return addressesId; }
	public void setAddressesId(Long addressesId) { this.addressesId = addressesId; }

	public GoalEntity getGoal() {
		return goal;
	}

	public void setGoal(GoalEntity goal) {
		this.goal = goal;
	}

	@Override
	public Long getId() {
		return this.addressesId;
	}


}
