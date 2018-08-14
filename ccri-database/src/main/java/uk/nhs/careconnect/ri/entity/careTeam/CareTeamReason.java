package uk.nhs.careconnect.ri.entity.careTeam;

import uk.nhs.careconnect.ri.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;

import javax.persistence.*;

@Entity
@Table(name="CareTeamIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_CARE_TEAM_IDENTIFIER", columnNames={"CARE_TEAM_IDENTIFIER_ID"})
		,indexes = {}
		)
public class CareTeamReason extends BaseResource {

	public CareTeamReason() {
	}
    public CareTeamReason(CareTeamEntity team) {
		this.team = team;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CARE_TEAM_REASON_ID")
    private Long reasonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CARE_TEAM_ID",foreignKey= @ForeignKey(name="FK_CARE_TEAM_IDENTIFIER_CARE_TEAM_ID"))
	private CareTeamEntity team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CONCEPT_CONDITION",nullable = true,foreignKey= @ForeignKey(name="FK_CARE_TEAM_CONCEPT_CONDITION"))
	private ConditionEntity condition;


	public CareTeamEntity getCareTeam() {
		return team;
	}

	public void setCareTeam(CareTeamEntity team) {
		this.team = team;
	}

	public Long getReasonId() {
		return reasonId;
	}

	public void setReasonId(Long reasonId) {
		this.reasonId = reasonId;
	}

	public CareTeamEntity getTeam() {
		return team;
	}

	public void setTeam(CareTeamEntity team) {
		this.team = team;
	}

	public ConditionEntity getCondition() {
		return condition;
	}

	public void setCondition(ConditionEntity condition) {
		this.condition = condition;
	}

	@Override
	public Long getId() {
		return null;
	}
}
