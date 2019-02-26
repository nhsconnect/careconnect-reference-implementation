package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;
import uk.nhs.careconnect.ri.database.entity.messageDefinition.MessageDefinitionEntity;

import javax.persistence.*;


@Entity
@Table(name="MessageDefinitionTelecom", uniqueConstraints= @UniqueConstraint(name="PK_MESSAGE_DEFINITION_TELECOM", columnNames={"MESSAGE_DEFINITION_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_MESSAGE_DEFINITION_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_MESSAGE_DEFINITION_TELECOM_MESSAGE_DEFINITION_ID", columnList="MESSAGE_DEFINITION_ID")
		})
public class MessageDefinitionTelecom extends BaseContactPoint {

	public MessageDefinitionTelecom() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MESSAGE_DEFINITION_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_TELECOM_MESSAGE_DEFINITION_ID"))
	private MessageDefinitionEntity messageDefinition;

	public Long getTelecomId() {
		return telecomId;
	}

	public void setTelecomId(Long telecomId) {
		this.telecomId = telecomId;
	}

	public MessageDefinitionEntity getMessageDefinition() {
		return messageDefinition;
	}

	public void setMessageDefinition(MessageDefinitionEntity messageDefinition) {
		this.messageDefinition = messageDefinition;
	}
}
