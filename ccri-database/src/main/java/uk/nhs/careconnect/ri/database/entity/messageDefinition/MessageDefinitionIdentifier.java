package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="MessageDefinitionIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_MESSAGE_DEFINITION_IDENTIFIER", columnNames={"MESSAGE_DEFINITION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_MESSAGE_DEFINITION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_MESSAGE_DEFINITION_IDENTIFER_MESSAGE_DEFINITION_ID", columnList="MESSAGE_DEFINITION_ID")


		})
public class MessageDefinitionIdentifier extends BaseIdentifier {

	public MessageDefinitionIdentifier() {

	}

	public MessageDefinitionIdentifier(MessageDefinitionEntity conceptmapEntity) {
		this.messageDefinition = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MESSAGE_DEFINITION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_MESSAGE_DEFINITION_IDENTIFIER"))
	private MessageDefinitionEntity messageDefinition;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public MessageDefinitionEntity getMessageDefinition() {
		return messageDefinition;
	}

	public void setMessageDefinition(MessageDefinitionEntity conceptmap) {
		this.messageDefinition = conceptmap;
	}
}
