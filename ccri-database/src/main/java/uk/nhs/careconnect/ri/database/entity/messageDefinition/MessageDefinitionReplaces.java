package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;


@Entity
@Table(name="MessageDefinitionReplaces", uniqueConstraints= @UniqueConstraint(name="PK_MESSAGE_DEFINITION_REPLACES", columnNames={"MESSAGE_DEFINITION_REPLACES_ID"})
		)
public class MessageDefinitionReplaces extends BaseResource {

	public MessageDefinitionReplaces() {

	}

	public MessageDefinitionReplaces(MessageDefinitionEntity conceptmapEntity) {
		this.messageDefinition = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MESSAGE_DEFINITION_REPLACES_ID")
	private Long replacesId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_MESSAGE_DEFINITION_REPLACES"))
	private MessageDefinitionEntity messageDefinition;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "REPLACES_MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEF_MESSAGE_DEF_REPLACES_MSG"))
	private MessageDefinitionEntity replacesMessageDefinition;


    public Long getReplacesId() { return replacesId; }
	public void setReplacesId(Long replacesId) { this.replacesId = replacesId; }

	public MessageDefinitionEntity getMessageDefinition() {
		return messageDefinition;
	}

	public void setMessageDefinition(MessageDefinitionEntity conceptmap) {
		this.messageDefinition = conceptmap;
	}

	@Override
	public Long getId() {
		return this.replacesId;
	}

	public MessageDefinitionEntity getReplacesMessageDefinition() {
		return replacesMessageDefinition;
	}

	public void setReplacesMessageDefinition(MessageDefinitionEntity replacesMessageDefinition) {
		this.replacesMessageDefinition = replacesMessageDefinition;
	}
}
