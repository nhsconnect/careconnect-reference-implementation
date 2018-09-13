package uk.nhs.careconnect.ri.database.entity.encounter;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="EncounterIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_ENCOUNTER_IDENTIFIER", columnNames={"ENCOUNTER_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_ENCOUNTER_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")
				,
				@Index(name="IDX_ENCOUNTER_IDENTIFIER_ENCOUNTER_ID", columnList = "ENCOUNTER_ID")

		})
public class EncounterIdentifier extends BaseIdentifier {

	public EncounterIdentifier() {

	}

	public EncounterIdentifier(EncounterEntity encounter) {
		this.encounter = encounter;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ENCOUNTER_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_ENCOUNTER_IDENTIFIER"))
	private EncounterEntity encounter;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public EncounterEntity getEncounter() {
	        return this.encounter;
	}

	public void setEncounter(EncounterEntity encounter) {
	        this.encounter = encounter;
	}




}
