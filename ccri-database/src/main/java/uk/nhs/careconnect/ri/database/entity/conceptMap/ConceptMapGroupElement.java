package uk.nhs.careconnect.ri.database.entity.conceptMap;

import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="ConceptMapElement", uniqueConstraints= @UniqueConstraint(name="PK_CONCEPT_MAP_ELEMENT", columnNames={"CONCEPT_MAP_ELEMENT_ID"})
		)
public class ConceptMapGroupElement {

	public ConceptMapGroupElement() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONCEPT_MAP_ELEMENT_ID")
	private Long elementId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONCEPT_MAP_GROUP_ID",foreignKey= @ForeignKey(name="FK_CONCEPT_MAP_GROUP_CONCEPT_MAP_ELEMENT"))
	private ConceptMapGroup conceptMapGroup;

	@OneToMany(mappedBy="conceptMapGroupElement", targetEntity= ConceptMapGroupTarget.class)
	private List<ConceptMapGroupTarget> targets;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CODE_CONCEPT_ID")
	private ConceptEntity sourceCode;

	public Long getElementId() {
		return elementId;
	}

	public void setElementId(Long elementId) {
		this.elementId = elementId;
	}

	public ConceptMapGroup getConceptMapGroup() {
		return conceptMapGroup;
	}

	public void setConceptMapGroup(ConceptMapGroup conceptMapGroup) {
		this.conceptMapGroup = conceptMapGroup;
	}

	public List<ConceptMapGroupTarget> getTargets() {
		return targets;
	}

	public void setTargets(List<ConceptMapGroupTarget> targets) {
		this.targets = targets;
	}

	public ConceptEntity getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(ConceptEntity sourceCode) {
		this.sourceCode = sourceCode;
	}
}
