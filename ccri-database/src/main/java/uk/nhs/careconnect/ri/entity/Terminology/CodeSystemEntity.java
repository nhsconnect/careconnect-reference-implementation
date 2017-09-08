package uk.nhs.careconnect.ri.entity.Terminology;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name="CodeSystem", uniqueConstraints= {
		@UniqueConstraint(name="IDX_CS_CODESYSTEM", columnNames= {"CODE_SYSTEM_URI"})
	})
@Entity()
public class CodeSystemEntity extends BaseResource {

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CODESYSTEM_ID")
	private Long myPid;
	public Long getPID()
	{
		return this.myPid;
	}

	
	@Column(name="CODE_SYSTEM_URI", nullable=false)
	private String codeSystemUri;
	public String getCodeSystemUri() {
		return codeSystemUri;
	}
	public void setCodeSystemUri(String theCodeSystemUri) {
		codeSystemUri = theCodeSystemUri;
	}


	@Column(name="name", nullable=true)
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
	public List<ConceptEntity> getContents( ) {
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
