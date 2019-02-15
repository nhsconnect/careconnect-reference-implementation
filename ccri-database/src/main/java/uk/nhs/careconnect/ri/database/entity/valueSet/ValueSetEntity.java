package uk.nhs.careconnect.ri.database.entity.valueSet;


import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hl7.fhir.dstu3.model.Enumerations.PublicationStatus;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapTelecom;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="ValueSet", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_MAP", columnNames={"VALUESET_ID"}))
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ValueSetEntity extends BaseResource {

	private static final int MAX_DESC_LENGTH = 4096;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="VALUESET_ID")
	private Integer id;

	
	@Column(name="VALUESET_STRID")
	private String strId;	
	public void setStrId(String strId) { this.strId = strId; }
	public String getStrId() { return strId; }

	@Column(name = "url")
	private String url;

	// ValueSet IDENTIFIERS
	@OneToMany(mappedBy="valueSetEntity", targetEntity=ValueSetIdentifier.class)
	private List<ValueSetIdentifier> identifiers;


	@Column(name = "version")
	private String version;
		
		@Column(name = "title",length = MAX_DESC_LENGTH)
		private String title;

		
		@Column(name = "VALUESET_NAME")
		private String name;

		
		@Column(name = "status")
		private PublicationStatus status;


	@Column(name = "experimental")
	private Boolean experimental;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATETIME")
	private Date changeDateTime;

	@Column(name = "publisher")
	private String publisher;

	@OneToMany(mappedBy="valueSet", targetEntity= ValueSetTelecom.class)
	private Set<ValueSetTelecom> contacts = new HashSet<>();

	// Ignore usage context and jurisdiction for now


		
		@Column(name = "description",length = MAX_DESC_LENGTH)
		private String description;

	@Column(name = "immutable")
	private Boolean immutable;

	@Column(name = "purpose",length = MAX_DESC_LENGTH)
	private String purpose;

	@Column(name = "copyright",length = MAX_DESC_LENGTH)
	private String copyright;

	@Column(name = "extensible")
	private Boolean extensible;



	// ValueSet INCLUDES - typically a subset of SNOMED codes
		@OneToMany(mappedBy="valueSetEntity", targetEntity=ValueSetInclude.class)
		private List<ValueSetInclude> includes;

		
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "CODESYSTEM_PID",referencedColumnName = "CODESYSTEM_ID", foreignKey = @ForeignKey(name = "FK_VALUESET_PID_CS_PID"))
		private CodeSystemEntity myCodeSystem;

		
		@Column(name = "CODESYSTEM_URL")
		private String myCodeSystemUrl;

	public void setId(Integer id) { this.id = id; }
	//public Integer getId() { return id; }
	public Long getId() { return id.longValue(); }

	public void setName(String name)
	{  this.name = name; }
	public String getName()  {  return this.name;  }

	public void setTitle(String title)
	{  this.title = title; }
	public String getTitle()  {  return this.title;  }

	public void setStatus(PublicationStatus status)
	{  this.status = status; }
	public PublicationStatus getStatus()  {  return this.status;  }

	public CodeSystemEntity getCodeSystem() {
		return this.myCodeSystem;
	}
	public void setCodeSystem(CodeSystemEntity codeSystem) {
		this.myCodeSystem = codeSystem;
	}

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


	public void setDescription(String description)
	{  this.description = description; }
	public String getDescription()  {  return this.description;  }

	public void setUrl(String url)
	{  this.url = url; }
	public String getUrl()  {  return this.url;  }

	public String getTermCodeSystemUrl() {
		return this.myCodeSystemUrl;
	}
	public void setTermCodeSystemUrl(String codeSystemUrl)
	{
		this.myCodeSystemUrl = codeSystemUrl;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Boolean getExperimental() {
		return experimental;
	}

	public void setExperimental(Boolean experimental) {
		this.experimental = experimental;
	}

	public Date getChangeDateTime() {
		return changeDateTime;
	}

	public void setChangeDateTime(Date changeDateTime) {
		this.changeDateTime = changeDateTime;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}


	public Boolean getImmutable() {
		return immutable;
	}

	public void setImmutable(Boolean immutable) {
		this.immutable = immutable;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public Boolean getExtensible() {
		return extensible;
	}

	public void setExtensible(Boolean extensible) {
		this.extensible = extensible;
	}

	public CodeSystemEntity getMyCodeSystem() {
		return myCodeSystem;
	}

	public void setMyCodeSystem(CodeSystemEntity myCodeSystem) {
		this.myCodeSystem = myCodeSystem;
	}

	public String getMyCodeSystemUrl() {
		return myCodeSystemUrl;
	}

	public void setMyCodeSystemUrl(String myCodeSystemUrl) {
		this.myCodeSystemUrl = myCodeSystemUrl;
	}

	public Set<ValueSetTelecom> getContacts() {
		return contacts;
	}

	public void setContacts(Set<ValueSetTelecom> contacts) {
		this.contacts = contacts;
	}
}
