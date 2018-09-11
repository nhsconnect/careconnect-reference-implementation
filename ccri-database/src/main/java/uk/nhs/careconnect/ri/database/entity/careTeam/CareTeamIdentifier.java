package uk.nhs.careconnect.ri.database.entity.careTeam;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import javax.persistence.*;

@Entity
@Table(name="CareTeamIdentifier2", uniqueConstraints= @UniqueConstraint(name="PK_CARE_TEAM_IDENTIFIER", columnNames={"CARE_TEAM_IDENTIFIER_ID"})
		,indexes = {}
		)
public class CareTeamIdentifier extends BaseIdentifier {

	public CareTeamIdentifier() {
	}
    public CareTeamIdentifier(CareTeamEntity team) {
		this.team = team;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CARE_TEAM_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CARE_TEAM_ID",foreignKey= @ForeignKey(name="FK_CARE_TEAM_IDENTIFIER_CARE_TEAM_ID"))

    private CareTeamEntity team;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public CareTeamEntity getCareTeam() {
		return team;
	}

	public void setCareTeam(CareTeamEntity team) {
		this.team = team;
	}


}
