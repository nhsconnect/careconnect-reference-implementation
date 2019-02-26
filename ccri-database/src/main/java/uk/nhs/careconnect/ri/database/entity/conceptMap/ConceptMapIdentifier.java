package uk.nhs.careconnect.ri.database.entity.conceptMap;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="ConceptMapIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_CONCEPT_MAP_IDENTIFIER", columnNames={"CONCEPT_MAP_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_CONCEPT_MAP_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_CONCEPT_MAP_IDENTIFER_CONCEPT_MAP_ID", columnList="CONCEPT_MAP_ID")


		})
public class ConceptMapIdentifier extends BaseIdentifier {

	public ConceptMapIdentifier() {

	}

	public ConceptMapIdentifier(ConceptMapEntity conceptmapEntity) {
		this.conceptMap = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONCEPT_MAP_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONCEPT_MAP_ID",foreignKey= @ForeignKey(name="FK_CONCEPT_MAP_CONCEPT_MAP_IDENTIFIER"))
	private ConceptMapEntity conceptMap;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ConceptMapEntity getConceptMap() {
		return conceptMap;
	}

	public void setConceptMap(ConceptMapEntity conceptmap) {
		this.conceptMap = conceptmap;
	}
}
