package uk.nhs.careconnect.ri.database.entity.conceptMap;

import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import org.hl7.fhir.dstu3.model.Enumerations.ConceptMapEquivalence;

import javax.persistence.*;


@Entity
@Table(name="ConceptMapTarget", uniqueConstraints= @UniqueConstraint(name="PK_CONCEPT_MAP_TARGET", columnNames={"CONCEPT_MAP_TARGET_ID"})
		)
public class ConceptMapGroupTarget {

	public ConceptMapGroupTarget() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONCEPT_MAP_TARGET_ID")
	private Long targetId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONCEPT_MAP_GROUP_ID",foreignKey= @ForeignKey(name="FK_CONCEPT_MAP_GROUP_CONCEPT_MAP_TARGET"))
	private ConceptMapGroupElement conceptMapGroupElement;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CODE_CONCEPT_ID")
	private ConceptEntity targetCode;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="status", nullable = false)
	private ConceptMapEquivalence equivalenceCode;

	@Column(name = "COMMENT")
	private String comment;

	public Long getTargetId() {
		return targetId;
	//	ConceptMap conceptMap;
	//	conceptMap.getGroupFirstRep().getElementFirstRep().getTargetFirstRep().getEquivalence();
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public ConceptMapGroupElement getConceptMapGroupElement() {
		return conceptMapGroupElement;
	}

	public void setConceptMapGroupElement(ConceptMapGroupElement conceptMapGroupElement) {
		this.conceptMapGroupElement = conceptMapGroupElement;
	}

	public ConceptEntity getTargetCode() {
		return targetCode;
	}

	public void setTargetCode(ConceptEntity targetCode) {
		this.targetCode = targetCode;
	}

	public ConceptMapEquivalence getEquivalenceCode() {
		return equivalenceCode;
	}

	public void setEquivalenceCode(ConceptMapEquivalence equivalenceCode) {
		this.equivalenceCode = equivalenceCode;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
