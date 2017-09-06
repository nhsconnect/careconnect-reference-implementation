package uk.nhs.careconnect.ri.entity.Terminology;


import org.hl7.fhir.dstu3.model.Enumerations;
import uk.nhs.careconnect.ri.entity.BaseResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="ValueSet", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_MAP", columnNames={"VALUESET_ID"}))
public class ValueSetEntity extends BaseResource {
	
	

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="VALUESET_ID")
	private Integer id;	
	public void setId(Integer id) { this.id = id; }
	//public Integer getId() { return id; }
	
	@Column(name="VALUESET_STRID")
	private String strId;	
	public void setStrId(String strId) { this.strId = strId; }
	public String getStrId() { return strId; }


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modifiedDate", nullable = true)
	private Date updated;
	public Date getUpdatedDate() { return updated; }
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createdDate", nullable = true)
	private Date createdDate;
	public Date getCreatedDate() { return createdDate; }
	
		
		@Column(name = "title")
		private String title;
		public void setTitle(String title)
		{  this.title = title; }
		public String getTitle()  {  return this.title;  }
		
		@Column(name = "name")
		private String name;
		public void setName(String name)
		{  this.name = name; }
		public String getName()  {  return this.name;  }
		
		@Column(name = "status")
		private Enumerations.PublicationStatus status;
		public void setStatus(Enumerations.PublicationStatus status)
		{  this.status = status; }
		public Enumerations.PublicationStatus getStatus()  {  return this.status;  }
		
		@Column(name = "url")
		private String url;
		public void setUrl(String url)
		{  this.url = url; }
		public String getUrl()  {  return this.url;  }
		
		@Column(name = "description")
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
		
		
		// ValueSet CONTENT
		@OneToMany(mappedBy="valueSetEntity", targetEntity=ValueSetContent.class)
		private List<ValueSetContent> contents;
		public void setContents(List<ValueSetContent> contents) {
	        this.contents = contents;
	    }
		public List<ValueSetContent> getContents( ) {
			if (contents == null) {
		        this.contents = new ArrayList<ValueSetContent>();
		    }
	        return this.contents;
	    }
		public List<ValueSetContent> addContent(ValueSetContent pi) { 
			contents.add(pi);
			return contents; }
		public List<ValueSetContent> removeContent(ValueSetContent content){ 
			contents.remove(content); return contents; }
		
		@ManyToOne()
		@JoinColumn(name = "CODESYSTEM_PID",referencedColumnName = "CODESYSTEM_ID", foreignKey = @ForeignKey(name = "FK_VALUESET_PID_CS_PID"))
		private CodeSystemEntity myCodeSystemVersion;
		public CodeSystemEntity getTermCodeSystemVersion() {
			return this.myCodeSystemVersion;
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
