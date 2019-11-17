package uk.nhs.careconnect.ri.database.entity.graphDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;
import uk.nhs.careconnect.ri.database.entity.BaseR4ContactPoint;

import javax.persistence.*;


@Entity
@Table(name="GraphDefinitionTelecom", uniqueConstraints= @UniqueConstraint(name="PK_GRAPH_TELECOM", columnNames={"GRAPH_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_GRAPH_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_GRAPH_TELECOM_GRAPH_ID", columnList="GRAPH_ID")
		})
public class GraphDefinitionTelecom extends BaseR4ContactPoint {

	public GraphDefinitionTelecom() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "GRAPH_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "GRAPH_ID",foreignKey= @ForeignKey(name="FK_GRAPH_TELECOM_GRAPH_ID"))
	private GraphDefinitionEntity graph;

	public Long getTelecomId() {
		return telecomId;
	}

	public void setTelecomId(Long telecomId) {
		this.telecomId = telecomId;
	}

	public GraphDefinitionEntity getGraphDefinition() {
		return graph;
	}

	public void setGraphDefinition(GraphDefinitionEntity graph) {
		this.graph = graph;
	}
}
