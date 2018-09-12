package uk.nhs.careconnect.ri.database.entity.carePlan;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="CarePlanIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_CAREPLAN_IDENTIFIER", columnNames={"CAREPLAN_IDENTIFIER_ID"})
		,indexes = {}
		)
public class CarePlanIdentifier extends BaseIdentifier {

	public CarePlanIdentifier() {
	}
    public CarePlanIdentifier(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CAREPLAN_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CAREPLAN_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_IDENTIFIER_CAREPLAN_ID"))

    private CarePlanEntity carePlan;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public CarePlanEntity getCarePlan() {
		return carePlan;
	}

	public void setCarePlan(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}
}
