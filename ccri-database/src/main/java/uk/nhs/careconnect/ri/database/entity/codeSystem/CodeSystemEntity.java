package uk.nhs.careconnect.ri.database.entity.codeSystem;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.Enumerations;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.*;

@Table(name="CodeSystem", uniqueConstraints= {
		@UniqueConstraint(name="IDX_CS_CODESYSTEM", columnNames= {"CODE_SYSTEM_URI"})
	}
	)
@Entity()
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CodeSystemEntity extends BaseResource {

	private static final int MAX_DESC_LENGTH = 4096;

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CODESYSTEM_ID")
	private Long myId;
	
	
	@Column(name="CODE_SYSTEM_URI", nullable=false)
	private String codeSystemUri;
	

	@Column(name="CODESYSTEM_NAME", nullable=true)
	private String name;

	@Column(name = "version")
	private String version;

	@Column(name = "title",length = MAX_DESC_LENGTH)
	private String title;

	@Column(name = "status")
	private Enumerations.PublicationStatus status;

	@Column(name = "experimental")
	private Boolean experimental;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATETIME")
	private Date changeDateTime;

	@Column(name = "publisher")
	private String publisher;

	@OneToMany(mappedBy="codeSystemEntity", targetEntity= CodeSystemTelecom.class)
	private Set<CodeSystemTelecom> contacts = new HashSet<>();

	// Ignore usage context and jurisdiction for now


	@Column(name = "description",length = MAX_DESC_LENGTH)
	private String description;

	@Column(name = "purpose",length = MAX_DESC_LENGTH)
	private String purpose;

	@Column(name = "copyright",length = MAX_DESC_LENGTH)
	private String copyright;

	@Column(name="content")
	private CodeSystem.CodeSystemContentMode content;

	// ValueSet CONTENT

	@OneToMany(mappedBy="codeSystemEntity", fetch = FetchType.LAZY, targetEntity=ConceptEntity.class)
	private List<ConceptEntity> concepts;

	public Long getId()
	{
		return this.myId;
	}

	public String getName() {
		return name;
	}
	public void setName(String theName) {
		name = theName;
	}

	public String getCodeSystemUri() {
		return codeSystemUri;
	}
	public void setCodeSystemUri(String theCodeSystemUri) {
		codeSystemUri = theCodeSystemUri;
	}


	public void setConceptEntities(List<ConceptEntity> concepts) {
		this.concepts = concepts;
	}
	public List<ConceptEntity> getConcepts( ) {
		if (concepts == null) {
			this.concepts = new ArrayList<ConceptEntity>();
		}
		return this.concepts;
	}
	public List<ConceptEntity> addContent(ConceptEntity pi) {
		concepts.add(pi);
		return concepts; }
	public List<ConceptEntity> removeContent(ConceptEntity content){
		concepts.remove(content); return concepts; }

	public Long getMyId() {
		return myId;
	}

	public void setMyId(Long myId) {
		this.myId = myId;
	}

	public Enumerations.PublicationStatus getStatus() {
		return status;
	}

	public void setStatus(Enumerations.PublicationStatus status) {
		this.status = status;
	}

	public CodeSystem.CodeSystemContentMode getContent() {
		return content;
	}

	public void setContent(CodeSystem.CodeSystemContentMode content) {
		this.content = content;
	}

	public void setConcepts(List<ConceptEntity> concepts) {
		this.concepts = concepts;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public Set<CodeSystemTelecom> getContacts() {
		return contacts;
	}

	public void setContacts(Set<CodeSystemTelecom> contacts) {
		this.contacts = contacts;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
}
