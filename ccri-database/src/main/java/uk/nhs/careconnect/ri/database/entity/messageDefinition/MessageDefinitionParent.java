package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;


@Entity
@Table(name="MessageDefinitionParent", uniqueConstraints= @UniqueConstraint(name="PK_MESSAGE_DEFINITION_PARENT", columnNames={"MESSAGE_DEFINITION_PARENT_ID"})
		)
public class MessageDefinitionParent extends BaseResource {

	public MessageDefinitionParent() {

	}

	public MessageDefinitionParent(MessageDefinitionEntity conceptmapEntity) {
		this.messageDefinition = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MESSAGE_DEFINITION_PARENT_ID")
	private Long replacesId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_MESSAGE_DEFINITION_PARENT"))
	private MessageDefinitionEntity messageDefinition;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PARENT_MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEF_MESSAGE_DEF_PARENT_MSG"))
	private MessageDefinitionEntity replacesMessageDefinition;


    public Long getParentId() { return replacesId; }
	public void setParentId(Long replacesId) { this.replacesId = replacesId; }

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

	public MessageDefinitionEntity getParentMessageDefinition() {
		return replacesMessageDefinition;
	}

	public void setParentMessageDefinition(MessageDefinitionEntity replacesMessageDefinition) {
		this.replacesMessageDefinition = replacesMessageDefinition;
	}
}
