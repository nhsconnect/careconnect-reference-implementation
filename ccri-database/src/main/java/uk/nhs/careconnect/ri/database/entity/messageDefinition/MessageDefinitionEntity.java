package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.codesystems.MessageheaderResponseRequest;
import org.hl7.fhir.instance.model.Conformance;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "MessageDefinition")
public class MessageDefinitionEntity extends BaseResource {

	/*

Does not currently include target dependsOn TODO not required at present

ditto for target product

	 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MESSAGE_DEFINITION_ID")
	private Long id;

	@Column(name = "URL")
	private String url;

	@OneToMany(mappedBy="messageDefinition", targetEntity= MessageDefinitionIdentifier.class)
	private List<MessageDefinitionIdentifier> identifiers;

	@Column(name = "VERSION")
	private String version;

	@Column(name = "NAME")
	private String name;

	@Column(name = "TITLE")
	private String title;

	// Not yet implemented replaces

	@Enumerated(EnumType.ORDINAL)
	@Column(name="status", nullable = false)
	Enumerations.PublicationStatus status;

	@Column(name="EXPERIMENTAL")
	private Boolean experimental;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CHANGE_DATE")
	private Date changedDate;

	@Column(name = "PUBLISHER")
	private String publisher;

	@OneToMany(mappedBy="messageDefinition", targetEntity= MessageDefinitionTelecom.class)
	private List<MessageDefinitionTelecom> contacts;

	@Column(name = "DESCRIPTION")
	private String description;

	// useContext .. implement if required

	// jurisdiction ... hard code to UK

	@Column(name = "PURPOSE")
	private String purpose;

	@Column(name = "COPYRIGHT")
	private String copyright;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "BASE_MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_BASE_MESSAGE_DEFINITION_ID"))
	private MessageDefinitionEntity baseMessageDefinition;

	// Not yet implemented base message/activity

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EVENT_CONCEPT",foreignKey= @ForeignKey(name="FK_MESSAGE_EVENT_CONCEPT"))
	ConceptEntity eventCode;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="CATEGORY", nullable = false)
	Conformance.MessageSignificanceCategory category;

	@OneToMany(mappedBy="messageDefinition", targetEntity= MessageDefinitionFocus.class)
	private List<MessageDefinitionFocus> foci;

	@OneToMany(mappedBy="messageDefinition", targetEntity= MessageDefinitionAllowedResponse.class)
	private List<MessageDefinitionAllowedResponse> allowedResponses;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="RESPONSE_REQUIRED", nullable = false)
	MessageheaderResponseRequest responseRequired;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	
	// MessageDefinition IDENTIFIERS

	public void setIdentifiers(List<MessageDefinitionIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	public List<MessageDefinitionIdentifier> getIdentifiers( ) {
		if (identifiers == null) {
			identifiers = new ArrayList<MessageDefinitionIdentifier>();
		}
		return this.identifiers;
	}
	public List<MessageDefinitionIdentifier> addIdentifier(MessageDefinitionIdentifier pi) {
		identifiers.add(pi);
		return identifiers; }

	public List<MessageDefinitionIdentifier> removeIdentifier(MessageDefinitionIdentifier identifier){
		identifiers.remove(identifiers); return identifiers; }

	// MessageDefinition Address


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Enumerations.PublicationStatus getStatus() {
		return status;
	}

	public void setStatus(Enumerations.PublicationStatus status) {
		this.status = status;
	}

	public Boolean getExperimental() {
		return experimental;
	}

	public void setExperimental(Boolean experimental) {
		this.experimental = experimental;
	}

	public Date getChangedDate() {
		return changedDate;
	}

	public void setChangedDate(Date changedDate) {
		this.changedDate = changedDate;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public List<MessageDefinitionTelecom> getContacts() {
		return contacts;
	}

	public void setContacts(List<MessageDefinitionTelecom> contacts) {
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

	public MessageDefinitionEntity getBaseMessageDefinition() {
		return baseMessageDefinition;
	}

	public void setBaseMessageDefinition(MessageDefinitionEntity baseMessageDefinition) {
		this.baseMessageDefinition = baseMessageDefinition;
	}

	public ConceptEntity getEventCode() {
		return eventCode;
	}

	public void setEventCode(ConceptEntity eventCode) {
		this.eventCode = eventCode;
	}

	public MessageheaderResponseRequest getResponseRequired() {
		return responseRequired;
	}

	public void setResponseRequired(MessageheaderResponseRequest responseRequired) {
		this.responseRequired = responseRequired;
	}

	public List<MessageDefinitionFocus> getFoci() {
		return foci;
	}

	public void setFoci(List<MessageDefinitionFocus> foci) {
		this.foci = foci;
	}

	public List<MessageDefinitionAllowedResponse> getAllowedResponses() {
		return allowedResponses;
	}

	public void setAllowedResponses(List<MessageDefinitionAllowedResponse> allowedResponses) {
		this.allowedResponses = allowedResponses;
	}

	public Conformance.MessageSignificanceCategory getCategory() {
		return category;
	}

	public void setCategory(Conformance.MessageSignificanceCategory category) {
		this.category = category;
	}
}
