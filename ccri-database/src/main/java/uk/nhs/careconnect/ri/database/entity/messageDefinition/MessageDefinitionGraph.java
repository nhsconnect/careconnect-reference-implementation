package uk.nhs.careconnect.ri.database.entity.messageDefinition;

import org.hl7.fhir.dstu3.model.ResourceType;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;


@Entity
@Table(name="MessageDefinitionGraph", uniqueConstraints= @UniqueConstraint(name="PK_MESSAGE_DEFINITION_GRAPH", columnNames={"MESSAGE_DEFINITION_GRAPH_ID"})
		)
public class MessageDefinitionGraph extends BaseResource {

	public MessageDefinitionGraph() {

	}

	public MessageDefinitionGraph(MessageDefinitionEntity conceptmapEntity) {
		this.messageDefinition = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MESSAGE_DEFINITION_GRAPH_ID")
	private Long graphId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MESSAGE_DEFINITION_ID",foreignKey= @ForeignKey(name="FK_MESSAGE_DEFINITION_MESSAGE_DEFINITION_GRAPH"))
	private MessageDefinitionEntity messageDefinition;

	

	@Column(name = "GRAPH")
	private String graph;



    public Long getGraphId() { return graphId; }
	public void setGraphId(Long graphId) { this.graphId = graphId; }

	public MessageDefinitionEntity getMessageDefinition() {
		return messageDefinition;
	}

	public void setMessageDefinition(MessageDefinitionEntity conceptmap) {
		this.messageDefinition = conceptmap;
	}

	@Override
	public Long getId() {
		return this.graphId;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}
}
