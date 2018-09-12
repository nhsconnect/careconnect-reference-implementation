package uk.nhs.careconnect.ri.database.entity.Terminology;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name="CodeSystem", uniqueConstraints= {
		@UniqueConstraint(name="IDX_CS_CODESYSTEM", columnNames= {"CODE_SYSTEM_URI"})
	}
	)
@Entity()
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CodeSystemEntity extends BaseResource {

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CODESYSTEM_ID")
	private Long myId;
	public Long getId()
	{
		return this.myId;
	}

	
	@Column(name="CODE_SYSTEM_URI", nullable=false)
	private String codeSystemUri;
	public String getCodeSystemUri() {
		return codeSystemUri;
	}
	public void setCodeSystemUri(String theCodeSystemUri) {
		codeSystemUri = theCodeSystemUri;
	}


	@Column(name="CODESYSTEM_NAME", nullable=true)
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String theName) {
		name = theName;
	}

	// ValueSet CONTENT

	@OneToMany(mappedBy="codeSystemEntity", targetEntity=ConceptEntity.class)
	private List<ConceptEntity> conceptEntities;


	public void setConceptEntities(List<ConceptEntity> conceptEntities) {
		this.conceptEntities = conceptEntities;
	}
	public List<ConceptEntity> getConcepts( ) {
		if (conceptEntities == null) {
			this.conceptEntities = new ArrayList<ConceptEntity>();
		}
		return this.conceptEntities;
	}
	public List<ConceptEntity> addContent(ConceptEntity pi) {
		conceptEntities.add(pi);
		return conceptEntities; }
	public List<ConceptEntity> removeContent(ConceptEntity content){
		conceptEntities.remove(content); return conceptEntities; }
}
