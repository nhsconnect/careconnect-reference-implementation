package uk.nhs.careconnect.ri.database.entity.carePlan;

import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import javax.persistence.*;

@Entity
@Table(name="CarePlanTeam", uniqueConstraints= @UniqueConstraint(name="PK_CAREPLAN_TEAM", columnNames={"CAREPLAN_TEAM_ID"})
		,indexes = {}
		)
public class CarePlanTeam {

	public CarePlanTeam() {
	}
    public CarePlanTeam(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CAREPLAN_TEAM_ID")
    private Long carePlanTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CAREPLAN_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_TEAM_CAREPLAN_ID"))
    private CarePlanEntity carePlan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="TEAM_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_TEAM_TEAM_ID"))
	private CareTeamEntity team;

	public CarePlanEntity getCarePlan() {
		return carePlan;
	}

	public void setCarePlan(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}

	public Long getCarePlanTeam() {
		return carePlanTeam;
	}

	public void setCarePlanTeam(Long carePlanTeam) {
		this.carePlanTeam = carePlanTeam;
	}

	public CareTeamEntity getTeam() {
		return team;
	}

	public void setTeam(CareTeamEntity team) {
		this.team = team;
	}
}
