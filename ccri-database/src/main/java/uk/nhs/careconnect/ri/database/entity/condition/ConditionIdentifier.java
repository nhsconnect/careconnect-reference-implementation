package uk.nhs.careconnect.ri.database.entity.condition;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="ConditionIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_CONDITION_IDENTIFIER", columnNames={"CONDITION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_CONDITION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class ConditionIdentifier extends BaseIdentifier {

	public ConditionIdentifier() {

	}

	public ConditionIdentifier(ConditionEntity condition) {
		this.condition = condition;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONDITION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONDITION_ID",foreignKey= @ForeignKey(name="FK_CONDITION_CONDITION_IDENTIFIER"))
	private ConditionEntity condition;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ConditionEntity getCondition() {
	        return this.condition;
	}

	public void setCondition(ConditionEntity condition) {
	        this.condition = condition;
	}




}
