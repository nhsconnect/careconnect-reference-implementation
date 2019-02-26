package uk.nhs.careconnect.ri.database.entity.conceptMap;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;

import javax.persistence.*;


@Entity
@Table(name="ConceptMapTelecom", uniqueConstraints= @UniqueConstraint(name="PK_CONCEPT_MAP_TELECOM", columnNames={"CONCEPT_MAP_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_CONCEPT_MAP_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_CONCEPT_MAP_TELECOM_CONCEPT_MAP_ID", columnList="CONCEPT_MAP_ID")
		})
public class ConceptMapTelecom extends BaseContactPoint {

	public ConceptMapTelecom() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONCEPT_MAP_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONCEPT_MAP_ID",foreignKey= @ForeignKey(name="FK_CONCEPT_MAP_TELECOM_CONCEPT_MAP_ID"))
	private ConceptMapEntity conceptMap;

	public Long getTelecomId() {
		return telecomId;
	}

	public void setTelecomId(Long telecomId) {
		this.telecomId = telecomId;
	}

	public ConceptMapEntity getConceptMap() {
		return conceptMap;
	}

	public void setConceptMap(ConceptMapEntity conceptMap) {
		this.conceptMap = conceptMap;
	}
}
