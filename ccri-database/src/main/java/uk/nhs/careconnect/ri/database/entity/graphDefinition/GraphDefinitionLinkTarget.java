package uk.nhs.careconnect.ri.database.entity.graphDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="GraphDefinitionLinkTarget", uniqueConstraints= @UniqueConstraint(name="PK_GRAPH_TARGET_LINK", columnNames={"GRAPH_LINK_TARGET_ID"})
		,indexes = {}
		)
public class GraphDefinitionLinkTarget extends BaseResource {

	public GraphDefinitionLinkTarget() {
	}


	private static final int MAX_DESC_LENGTH = 4096;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "GRAPH_LINK_TARGET_ID")
	private Long graphDefinitionLinkTarget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "GRAPH_LINK_ID",foreignKey= @ForeignKey(name="FK_GRAPH_LINK_TARGET_ITEM_ID"))
	private GraphDefinitionLink graphDefinitionLink;


	@Column(name="RESOURCE_TYPE")
	String type;

	@Column(name= "PARAMS")
	private String params;

	@Column(name = "PROFILE")
	private String profile;

	@Column(name="DESCRIPTION",length = MAX_DESC_LENGTH,nullable = true)
	private String description;

	@Column(name= "TARGET_ID")
	private String targetId;

	@OneToMany(mappedBy="graphDefinitionLinkTarget", targetEntity=GraphDefinitionLink.class)
	@OrderBy(value = "graphLink ASC")
	private Set<GraphDefinitionLink> links = new HashSet<>();

	@OneToMany(mappedBy="graphDefinitionLinkTarget", targetEntity=GraphDefinitionLinkTargetCompartment.class)
	private Set<GraphDefinitionLinkTargetCompartment> compartments = new HashSet<>();

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}


	@Override
	public Long getId() {
		return null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getGraphDefinitionLinkTarget() {
		return graphDefinitionLinkTarget;
	}

	public void setGraphDefinitionLinkTarget(Long graphDefinitionLinkTarget) {
		this.graphDefinitionLinkTarget = graphDefinitionLinkTarget;
	}

	public GraphDefinitionLink getGraphDefinitionLink() {
		return graphDefinitionLink;
	}

	public void setGraphDefinitionLink(GraphDefinitionLink graphDefinitionLink) {
		this.graphDefinitionLink = graphDefinitionLink;
	}

	public Set<GraphDefinitionLink> getLinks() {
		return links;
	}

	public void setLinks(Set<GraphDefinitionLink> links) {
		this.links = links;
	}

	public Set<GraphDefinitionLinkTargetCompartment> getCompartments() {
		return compartments;
	}

	public void setCompartments(Set<GraphDefinitionLinkTargetCompartment> compartments) {
		this.compartments = compartments;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
}
