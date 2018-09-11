package uk.nhs.careconnect.ri.database.entity.Terminology;


import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hl7.fhir.dstu3.model.Enumerations.PublicationStatus;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="ValueSet", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_MAP", columnNames={"VALUESET_ID"}))
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ValueSetEntity extends BaseResource {
	
	

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="VALUESET_ID")
	private Integer id;
	public void setId(Integer id) { this.id = id; }
	//public Integer getId() { return id; }
	public Long getId() { return id.longValue(); }
	
	@Column(name="VALUESET_STRID")
	private String strId;	
	public void setStrId(String strId) { this.strId = strId; }
	public String getStrId() { return strId; }

	
		
		@Column(name = "title")
		private String title;
		public void setTitle(String title)
		{  this.title = title; }
		public String getTitle()  {  return this.title;  }
		
		@Column(name = "VALUESET_NAME")
		private String name;
		public void setName(String name)
		{  this.name = name; }
		public String getName()  {  return this.name;  }
		
		@Column(name = "status")
		private PublicationStatus status;
		public void setStatus(PublicationStatus status)
		{  this.status = status; }
		public PublicationStatus getStatus()  {  return this.status;  }
		
		@Column(name = "url")
		private String url;
		public void setUrl(String url)
		{  this.url = url; }
		public String getUrl()  {  return this.url;  }
		
		@Column(name = "description", length = 4096)
		private String description;
		public void setDescription(String description)
		{  this.description = description; }
		public String getDescription()  {  return this.description;  }
		
		
		// ValueSet IDENTIFIERS
		@OneToMany(mappedBy="valueSetEntity", targetEntity=ValueSetIdentifier.class)
		private List<ValueSetIdentifier> identifiers;
		public void setIdentifiers(List<ValueSetIdentifier> identifiers) {
	        this.identifiers = identifiers;
	    }
		public List<ValueSetIdentifier> getIdentifiers( ) {
			if (identifiers == null) {
		        identifiers = new ArrayList<ValueSetIdentifier>();
		    }
	        return this.identifiers;
	    }
		public List<ValueSetIdentifier> addIdentifier(ValueSetIdentifier pi) { 
			identifiers.add(pi);
			return identifiers; }
		public List<ValueSetIdentifier> removeIdentifier(ValueSetIdentifier identifier){ 
			identifiers.remove(identifier); return identifiers; }
		
		
		// ValueSet INCLUDES - typically a subset of SNOMED codes
		@OneToMany(mappedBy="valueSetEntity", targetEntity=ValueSetInclude.class)
		private List<ValueSetInclude> includes;
		public void setIncludes(List<ValueSetInclude> includes) {
	        this.includes = includes;
	    }
		public List<ValueSetInclude> getIncludes( ) {
			if (includes == null) {
		        this.includes = new ArrayList<ValueSetInclude>();
		    }
	        return this.includes;
	    }
		public List<ValueSetInclude> addInclude(ValueSetInclude pi) {
			includes.add(pi);
			return includes; }
		public List<ValueSetInclude> removeInclude(ValueSetInclude content){
			includes.remove(content); return includes; }
		
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "CODESYSTEM_PID",referencedColumnName = "CODESYSTEM_ID", foreignKey = @ForeignKey(name = "FK_VALUESET_PID_CS_PID"))
		private CodeSystemEntity myCodeSystem;
		public CodeSystemEntity getCodeSystem() {
			return this.myCodeSystem;
		}
		public void setCodeSystem(CodeSystemEntity codeSystem) {
			this.myCodeSystem = codeSystem;
		}
		
		@Column(name = "CODESYSTEM_URL")
		private String myCodeSystemUrl;
		public String getTermCodeSystemUrl() {
			return this.myCodeSystemUrl;
		}
		public void setTermCodeSystemUrl(String codeSystemUrl)
		{
			this.myCodeSystemUrl = codeSystemUrl; 
		}
		
	
		
}
