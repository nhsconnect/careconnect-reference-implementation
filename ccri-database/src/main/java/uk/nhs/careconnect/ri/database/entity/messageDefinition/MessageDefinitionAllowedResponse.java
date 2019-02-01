package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;


@Entity
@Table(name="MessageDefinitionAllowedResponse", uniqueConstraints= @UniqueConstraint(name="PK_MESSAGE_DEFINITION_ALLOWED_RESPONSE", columnNames={"MESSAGE_DEFINITION_ALLOWED_RESPONSE_ID"})
		)
public class MessageDefinitionAllowedResponse extends BaseResource {

	public MessageDefinitionAllowedResponse() {

	}

	public MessageDefinitionAllowedResponse(MessageDefinitionEntity conceptmapEntity) {
		this.messageDefinition = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MESSAGE_DEFINITION_ALLOWED_RESPONSE_ID")
	private Long allowedResponseId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_MESSAGE_DEFINITION_ALLOWED_RESPONSE"))
	private MessageDefinitionEntity messageDefinition;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "RESPONSE_MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEF_MESSAGE_DEF_ALLOWED_RESPONSE_MSG"))
	private MessageDefinitionEntity responseMessageDefinition;

	@Column(name= "SITUATION")
	private String situation;

    public Long getAllowedResponseId() { return allowedResponseId; }
	public void setAllowedResponseId(Long allowedResponseId) { this.allowedResponseId = allowedResponseId; }

	public MessageDefinitionEntity getMessageDefinition() {
		return messageDefinition;
	}

	public void setMessageDefinition(MessageDefinitionEntity conceptmap) {
		this.messageDefinition = conceptmap;
	}

	@Override
	public Long getId() {
		return this.allowedResponseId;
	}

	public MessageDefinitionEntity getResponseMessageDefinition() {
		return responseMessageDefinition;
	}

	public void setResponseMessageDefinition(MessageDefinitionEntity responseMessageDefinition) {
		this.responseMessageDefinition = responseMessageDefinition;
	}

	public String getSituation() {
		return situation;
	}

	public void setSituation(String situation) {
		this.situation = situation;
	}
}
