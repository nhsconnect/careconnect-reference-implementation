package uk.nhs.careconnect.ri.database.entity.Terminology;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name="ConceptParentChildLink", indexes= {
		// For data loading
		@Index(columnList = "relationshipId", name = "IDX_RELATIONSHIP")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ConceptParentChildLink {


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CHILD_CONCEPT_ID", nullable=false, referencedColumnName="CONCEPT_ID", foreignKey=@ForeignKey(name="FK_TERM_CONCEPTPC_CHILD"))
	private ConceptEntity child;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CODESYSTEM_ID", nullable=false, foreignKey=@ForeignKey(name="FK_TERM_CONCEPTPC_CS"))
	private CodeSystemEntity myCodeSystem;

	@ManyToOne(fetch = FetchType.LAZY, cascade= {})
	@JoinColumn(name="PARENT_CONCEPT_ID", nullable=false, referencedColumnName="CONCEPT_ID", foreignKey=@ForeignKey(name="FK_TERM_CONCEPTPC_PARENT"))
	private ConceptEntity parent;

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="CONCEPT_PARENT_CHILD_ID")
	private Long myPid;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="REL_TYPE", length=5, nullable=true)
	private RelationshipTypeEnum myRelationshipType;

	@Column(name="relationshipId")
	private String relationshipId;

	public ConceptEntity getChild() {
		return child;
	}

	public RelationshipTypeEnum getRelationshipType() {
		return myRelationshipType;
	}

	public CodeSystemEntity getCodeSystem() {
		return myCodeSystem;
	}
	
	public ConceptEntity getParent() {
		return parent;
	}

	public void setChild(ConceptEntity theChild) {
		child = theChild;
	}
	
	public void setCodeSystem(CodeSystemEntity theCodeSystem) {
		myCodeSystem = theCodeSystem;
	}

	public void setParent(ConceptEntity theParent) {
		parent = theParent;
	}

	public String getRelationshipId() {
		return relationshipId;
	}

	public ConceptParentChildLink setRelationshipId(String relationshipId) {
		this.relationshipId = relationshipId;
		return this;
	}

	public void setRelationshipType(RelationshipTypeEnum theRelationshipType) {
		myRelationshipType = theRelationshipType;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((myCodeSystem == null) ? 0 : myCodeSystem.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((myRelationshipType == null) ? 0 : myRelationshipType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConceptParentChildLink other = (ConceptParentChildLink) obj;
		if (child == null) {
			if (other.child != null)
				return false;
		} else if (!child.equals(other.child))
			return false;
		if (myCodeSystem == null) {
			if (other.myCodeSystem != null)
				return false;
		} else if (!myCodeSystem.equals(other.myCodeSystem))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (myRelationshipType != other.myRelationshipType)
			return false;
		return true;
	}


	public enum RelationshipTypeEnum{
		// ********************************************
		// IF YOU ADD HERE MAKE SURE ORDER IS PRESERVED
		ISA,
		FindingSite,
		SubSet
	}


	public Long getId() {
		return myPid;
	}
}
