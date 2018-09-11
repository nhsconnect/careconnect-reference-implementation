package uk.nhs.careconnect.ri.database.entity.Terminology;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.*;

// KGM This is a dumber version of the HAPI class. Just enough to enforce foreign key constraint.

@Entity
@Table(name="Concept", uniqueConstraints= {
	@UniqueConstraint(name="IDX_CONCEPT_CS_CODE", columnNames= {"CODESYSTEM_ID", "CODE"})
}, indexes= {
	@Index(name = "IDX_CONCEPT_INDEXSTATUS", columnList="INDEX_STATUS"),

		@Index(columnList = "CODE", name = "IDX_CODE")	,
		@Index(columnList = "DISPLAY", name = "IDX_DISPLAY")	,
		@Index(columnList = "CODE,CODESYSTEM_ID", name = "IDX_CODE_CODESYSTEM")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class 	ConceptEntity extends BaseResource {
	private static final int MAX_DESC_LENGTH = 400;


	@OneToMany(mappedBy = "parent", cascade= {})
	private Collection<ConceptParentChildLink> children;

	@Column(name = "CODE", length = 100, nullable = false)
	//@Column(name = "CODE", columnDefinition = "varchar(100) COLLATE utf8_bin NOT NULL")
	private String code;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CODESYSTEM_ID",referencedColumnName = "CODESYSTEM_ID", foreignKey = @ForeignKey(name = "FK_CONCEPT_PID_CS_PID"))
	private CodeSystemEntity codeSystemEntity;

	//@formatter:off
	@Column(name="DISPLAY", length=MAX_DESC_LENGTH, nullable=true)
	private String myDisplay;

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CONCEPT_ID")
	private Long conceptId;
	public Long getId() {
		return conceptId;
	}
	public ConceptEntity setId(Long id) {
		this.conceptId = id;
		return this;
	}

	@Column(name = "INDEX_STATUS", nullable = true)
	private Long myIndexStatus;

	@Transient
	private String myParentPids;

	@OneToMany(cascade = {}, mappedBy = "child")

	private Collection<ConceptParentChildLink> parents;

	@Column(name = "active")
	private Boolean active;

	@Column(name = "effectiveDate")
	private Date effectiveDate;

	@ManyToOne(fetch = FetchType.LAZY)

	@JoinColumn(name = "moduleId",foreignKey= @ForeignKey(name="FK_TERM_CONCEPT_MODULE"))
	private ConceptEntity moduleId;

	@ManyToOne(fetch = FetchType.LAZY)

	@JoinColumn (name = "definitionStatusId",foreignKey= @ForeignKey(name="FK_TERM_CONCEPT_DEFINITION"))
	private ConceptEntity definitionStatusId;

	@Column(name = "description", length=4096)
	private String description;

    @Column(name = "abstractCode")
    private Boolean abstractCode;

	@OneToMany(mappedBy="conceptEntity", targetEntity=ConceptDesignation.class)

	private List<ConceptDesignation> designations;



    public Boolean getAbstractCode() {
        return abstractCode;
    }

    public ConceptEntity setAbstractCode(Boolean abstractCode) {
        this.abstractCode = abstractCode;
        return this;
    }

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}
	public ConceptEntity setCode(String theCode) {
		code = theCode;
		return this;
	}

	public ConceptEntity(CodeSystemEntity codeSystemEntity,String theCode) {
		this.codeSystemEntity = codeSystemEntity;
		this.code = theCode;
	}

	public CodeSystemEntity getCodeSystem() {
		return this.codeSystemEntity;
	}
    public String getSystem() {
        return this.codeSystemEntity.getCodeSystemUri();
    }
	

	public String getDisplay() {
		return myDisplay;
	}
	public ConceptEntity setDisplay(String theDisplay) {
		myDisplay = theDisplay;
		if (theDisplay != null && !theDisplay.isEmpty() && theDisplay.length() > MAX_DESC_LENGTH) {
			myDisplay = myDisplay.substring(0, MAX_DESC_LENGTH);
		}
		return this;
	}


	public ConceptEntity() {
		super();
	}
	

	public void setActive(Boolean active) {   this.active = active;   }
	public Boolean getActive() { return this.active;  }
	

	public void setEffectiveDate(Date effectiveDate) {   this.effectiveDate = effectiveDate;   }
	public Date getEffectiveDate() { return this.effectiveDate;  }

	
	public ConceptEntity addChild(ConceptEntity theChild, ConceptParentChildLink.RelationshipTypeEnum theRelationshipType) {
		Validate.notNull(theRelationshipType, "theRelationshipType must not be null");
		ConceptParentChildLink link = new ConceptParentChildLink();
		link.setParent(this);
		link.setChild(theChild);
		// KGM link.setRelationshipType(theRelationshipType);
		getChildren().add(link);

		theChild.getParents().add(link);
		return this;
	}

	public void addChildren(List<ConceptEntity> theChildren, ConceptParentChildLink.RelationshipTypeEnum theRelationshipType) {
		for (ConceptEntity next : theChildren) {
			addChild(next, theRelationshipType);
		}
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof ConceptEntity)) {
			return false;
		}
		if (theObj == this) {
			return true;
		}

		ConceptEntity obj = (ConceptEntity) theObj;

		EqualsBuilder b = new EqualsBuilder();
		b.append(codeSystemEntity, obj.codeSystemEntity);
		b.append(code, obj.code);
		return b.isEquals();
	}

	public Collection<ConceptParentChildLink> getChildren() {
		if (children == null) {
			children = new ArrayList<ConceptParentChildLink>();
		}
		return children;
	}

	
	
	

	public Long getIndexStatus() {
		return myIndexStatus;
	}

	public String getParentPidsAsString() {
		return myParentPids;
	}

	public Collection<ConceptParentChildLink> getParents() {
		if (parents == null) {
			parents = new ArrayList<ConceptParentChildLink>();
		}
		return parents;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder();
		b.append(codeSystemEntity);
		b.append(code);
		return b.toHashCode();
	}

	private void parentPids(ConceptEntity theNextConcept, Set<Long> theParentPids) {
		for (ConceptParentChildLink nextParentLink : theNextConcept.getParents()) {
			ConceptEntity parent = nextParentLink.getParent();
			Long parentConceptId = parent.getId();
			Validate.notNull(parentConceptId);
			if (parent != null && theParentPids.add(parentConceptId)) {
				parentPids(parent, theParentPids);
			}
		}
	}

	@PreUpdate
	@PrePersist
	public void prePersist() {
		Set<Long> parentPids = new HashSet<Long>();
		ConceptEntity entity = this;
		parentPids(entity, parentPids);
		entity.setParentPids(parentPids);

		//ourLog.trace("Code {}/{} has parents {}", entity.getId(), entity.getCode(), entity.getParentPidsAsString());
	}

    public List<ConceptDesignation> getDesignations() {
        if (designations == null) {
            designations = new ArrayList<ConceptDesignation>();
        }
        return designations;
    }

    public void setDesignations(List<ConceptDesignation> designations) {
        this.designations = designations;
    }

    public ConceptEntity setCodeSystem(CodeSystemEntity theCodeSystem) {
		this.codeSystemEntity = theCodeSystem;
		return this;
	}


	public void setIndexStatus(Long theIndexStatus) {
		myIndexStatus = theIndexStatus;
	}

	public void setParentPids(Set<Long> theParentPids) {
		StringBuilder b = new StringBuilder();
		for (Long next : theParentPids) {
			if (b.length() > 0) {
				b.append(' ');
			}
			b.append(next);
		}

		if (b.length() == 0) {
			b.append("NONE");
		}

		myParentPids = b.toString();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("code", code).append("display", myDisplay).build();
	}
}
