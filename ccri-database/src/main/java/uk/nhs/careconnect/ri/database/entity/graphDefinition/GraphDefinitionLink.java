package uk.nhs.careconnect.ri.database.entity.graphDefinition;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="GraphDefinitionLink", uniqueConstraints= @UniqueConstraint(name="PK_GRAPH_LINK", columnNames={"GRAPH_LINK_ID"})
		,indexes = {}
		)
public class GraphDefinitionLink extends BaseResource {

	public GraphDefinitionLink() {
	}
    public GraphDefinitionLink(GraphDefinitionEntity graph) {
		this.graph = graph;
	}

	private static final int MAX_DESC_LENGTH = 4096;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "GRAPH_LINK_ID")
    private Long graphLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "GRAPH_ID",foreignKey= @ForeignKey(name="FK_GRAPH_LINK_GRAPH_ID"))
	private GraphDefinitionEntity graph;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "GRAPH_TARGET_ID",foreignKey= @ForeignKey(name="FK_GRAPH_LINK_GRAPH_TARGET_ID"))
	private GraphDefinitionLinkTarget graphDefinitionLinkTarget;

	@Column(name="PATH",nullable = false)
	private String path;

	@Column(name="SLICE",length = MAX_DESC_LENGTH,nullable = true)
	private String slice;

	@Column(name= "MIN_INT")
	private Integer minimum;

	@Column(name= "MAX")
	private String maximum;

	@Column(name="DESCRIPTION",length = MAX_DESC_LENGTH,nullable = true)
	private String description;

	@OneToMany(mappedBy="graphDefinitionLink", fetch = FetchType.LAZY, targetEntity= GraphDefinitionLinkTarget.class)
	private Set<GraphDefinitionLinkTarget> targets = new HashSet<>();


	public GraphDefinitionEntity getGraph() {
		return graph;
	}

	public void setGraph(GraphDefinitionEntity graph) {
		this.graph = graph;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSlice() {
		return slice;
	}

	public void setSlice(String slice) {
		this.slice = slice;
	}

	public Integer getMinimum() {
		return minimum;
	}

	public void setMinimum(Integer minimum) {
		this.minimum = minimum;
	}

	public String getMaximum() {
		return maximum;
	}

	public void setMaximum(String maximum) {
		this.maximum = maximum;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GraphDefinitionEntity getGraphDefinition() {
		return graph;
	}

	public void setGraphDefinition(GraphDefinitionEntity graph) {
		this.graph = graph;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public GraphDefinitionEntity getForm() {
		return graph;
	}

	public void setForm(GraphDefinitionEntity graph) {
		this.graph = graph;
	}

	@Override
	public Long getId() {
		return null;
	}

	public Long getGraphLink() {
		return graphLink;
	}

	public void setGraphLink(Long graphLink) {
		this.graphLink = graphLink;
	}

	public GraphDefinitionLinkTarget getGraphDefinitionLinkTarget() {
		return graphDefinitionLinkTarget;
	}

	public void setGraphDefinitionLinkTarget(GraphDefinitionLinkTarget graphDefinitionLinkTarget) {
		this.graphDefinitionLinkTarget = graphDefinitionLinkTarget;
	}

	public Set<GraphDefinitionLinkTarget> getTargets() {
		return targets;
	}

	public void setTargets(Set<GraphDefinitionLinkTarget> targets) {
		this.targets = targets;
	}
}
