package uk.nhs.careconnect.ri.database.entity.structureDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="StructureDefinitionIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_STRUCTURE_DEFINITION_IDENTIFIER", columnNames={"STRUCTURE_DEFINITION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_STRUCTURE_DEFINITION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_STRUCTURE_DEFINITION_IDENTIFER_STRUCTURE_DEFINITION_ID", columnList="STRUCTURE_DEFINITION_ID")


		})
public class StructureDefinitionIdentifier extends BaseIdentifier {

	public StructureDefinitionIdentifier() {

	}

	public StructureDefinitionIdentifier(StructureDefinitionEntity structureDefinitionEntity) {
		this.structureDefinition = structureDefinitionEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "STRUCTURE_DEFINITION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "STRUCTURE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_STRUCTURE_DEFINITION_STRUCTURE_DEFINITION_IDENTIFIER"))
	private StructureDefinitionEntity structureDefinition;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public StructureDefinitionEntity getStructuredDefinition() {
		return structureDefinition;
	}

	public void setStructuredDefinition(StructureDefinitionEntity structureDefinition) {
		this.structureDefinition = structureDefinition;
	}
}
