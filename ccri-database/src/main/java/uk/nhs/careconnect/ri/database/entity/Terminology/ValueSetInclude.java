package uk.nhs.careconnect.ri.database.entity.Terminology;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


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




	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CODESYSTEM_PID",referencedColumnName = "CODESYSTEM_ID", foreignKey = @ForeignKey(name = "FK_VALUESET_INCLUDE_CODESYSTEM_ID"))
	private CodeSystemEntity myCodeSystem;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUESET_ID",foreignKey= @ForeignKey(name="FK_VALUESET_VALUESET_INLCUDE"))
	private ValueSetEntity valueSetEntity;

    @OneToMany(mappedBy="include", targetEntity=ValueSetIncludeConcept.class)
    private List<ValueSetIncludeConcept> concepts;

	@OneToMany(mappedBy="include", targetEntity=ValueSetIncludeFilter.class)
	private List<ValueSetIncludeFilter> filters;

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

    public List<ValueSetIncludeConcept> getConcepts() {
	    if (concepts == null) {
	        concepts = new ArrayList<ValueSetIncludeConcept>() ;

        }
        return concepts;
    }

    public void setConcepts(List<ValueSetIncludeConcept> concepts) {
        this.concepts = concepts;
    }

	public List<ValueSetIncludeFilter> getFilters() {
		if (filters == null) {
			filters = new ArrayList<ValueSetIncludeFilter>() ;

		}
		return filters;
	}

	public void setFilters(List<ValueSetIncludeFilter> filters) {
		this.filters = filters;
	}
}
