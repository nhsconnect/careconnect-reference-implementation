package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.codesystems.MessageheaderResponseRequest;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;


@Entity
@Table(name="MessageDefinitionFocus", uniqueConstraints= @UniqueConstraint(name="PK_MESSAGE_DEFINITION_FOCUS", columnNames={"MESSAGE_DEFINITION_FOCUS_ID"})
		)
public class MessageDefinitionFocus extends BaseResource {

	public MessageDefinitionFocus() {

	}

	public MessageDefinitionFocus(MessageDefinitionEntity conceptmapEntity) {
		this.messageDefinition = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MESSAGE_DEFINITION_FOCUS_ID")
	private Long focusId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_MESSAGE_DEFINITION_FOCUS"))
	private MessageDefinitionEntity messageDefinition;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="RESOURCE_TYPE")
	ResourceType resourceType;

	@Column(name = "PROFILE")
	private String profile;

	@Column(name= "MIN_INT")
	private Integer minimum;

	@Column(name= "MAX")
	private String maximum;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CONCEPT",foreignKey= @ForeignKey(name="FK_MESSAGE_FOCUS_CONCEPT"))
	ConceptEntity code;

    public Long getFocusId() { return focusId; }
	public void setFocusId(Long focusId) { this.focusId = focusId; }

	public MessageDefinitionEntity getMessageDefinition() {
		return messageDefinition;
	}

	public void setMessageDefinition(MessageDefinitionEntity conceptmap) {
		this.messageDefinition = conceptmap;
	}

	@Override
	public Long getId() {
		return this.focusId;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getMaximum() {
		return maximum;
	}

	public void setMaximum(String maximum) {
		this.maximum = maximum;
	}

	public Integer getMinimum() {
		return minimum;
	}

	public void setMinimum(Integer minimum) {
		this.minimum = minimum;
	}
}
