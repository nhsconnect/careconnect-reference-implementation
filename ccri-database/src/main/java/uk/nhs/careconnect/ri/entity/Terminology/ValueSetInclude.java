package uk.nhs.careconnect.ri.entity.Terminology;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name="ValueSetInclude", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_INCLUDE", columnNames={"VALUESET_INCLUDE_ID"}))
public class ValueSetInclude {
	
	private static final int MAX_DESC_LENGTH = 400;
	
	public ValueSetInclude() {
		
	}
	
	public ValueSetInclude(ValueSetEntity valueSetEntity) {
		this.valueSetEntity = valueSetEntity;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "VALUESET_INCLUDE_ID")
	private Integer contentId;



	@ElementCollection
	@CollectionTable(name = "ValueSetIncludeConcepts", joinColumns = @JoinColumn(name = "VALUESET_INCLUDE_ID"))
	private Set<ConceptEntity> concepts = new HashSet<ConceptEntity>();

	@ManyToOne()
	@JoinColumn(name = "CODESYSTEM_PID",referencedColumnName = "CODESYSTEM_ID", foreignKey = @ForeignKey(name = "FK_VALUESET_INCLUDE_CODESYSTEM_ID"))
	private CodeSystemEntity myCodeSystem;


	@ManyToOne
	@JoinColumn (name = "VALUESET_ID",foreignKey= @ForeignKey(name="FK_VALUESET_VALUESET_INLCUDE"))
	private ValueSetEntity valueSetEntity;

	// Don't allow access to CodeSystem. This is not the place
	public String getSystem() {
		return this.myCodeSystem.getCodeSystemUri();
	}
	public void setCodeSystem(CodeSystemEntity codeSystem) {
		this.myCodeSystem = codeSystem;
	}


	public Integer getId() { return contentId; }
	public void setId(Integer contentId) { this.contentId = contentId; }

	public ValueSetEntity getValueSetEntity() {
	        return this.valueSetEntity;
	}
	public void setValueSetEntity(ValueSetEntity valueSetEntity) {
	        this.valueSetEntity = valueSetEntity;
	}

	public Set<ConceptEntity> getConcepts() {
		return concepts;
	}
	public void setConcepts(Set<ConceptEntity> concepts) {
		this.concepts = concepts;
	}
}
