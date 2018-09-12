package uk.nhs.careconnect.ri.database.entity.carePlan;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="CarePlanActivity", uniqueConstraints= @UniqueConstraint(name="PK_CAREPLAN_ACTIVITY", columnNames={"CAREPLAN_ACTIVITY_ID"})
		,indexes = {}
		)
public class CarePlanActivity  {

	public CarePlanActivity() {
	}
    public CarePlanActivity(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CAREPLAN_ACTIVITY_ID")
    private Long carePlanActivity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CAREPLAN_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_ACTIVITY_CAREPLAN_ID"))

    private CarePlanEntity carePlan;

	@OneToMany(mappedBy="carePlanActivity", targetEntity=CarePlanActivityDetail.class)
	private Set<CarePlanActivityDetail> details = new HashSet<>();

	public CarePlanEntity getCarePlan() {
		return carePlan;
	}

	public void setCarePlan(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}

	public Long getCarePlanActivity() {
		return carePlanActivity;
	}

	public void setCarePlanActivity(Long carePlanActivity) {
		this.carePlanActivity = carePlanActivity;
	}

	public Set<CarePlanActivityDetail> getDetails() {
		return details;
	}

	public void setDetails(Set<CarePlanActivityDetail> details) {
		this.details = details;
	}
}
