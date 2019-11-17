package uk.nhs.careconnect.ri.database.entity.graphDefinition;

import org.hl7.fhir.r4.model.GraphDefinition;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import javax.persistence.*;

@Entity
@Table(name="GraphDefinitionCompartment", uniqueConstraints= @UniqueConstraint(name="PK_GGRAPH_COMPARTMENT", columnNames={"GRAPH_COMPARTMENT_ID"})
		,indexes = {}
		)
public class GraphDefinitionLinkTargetCompartment extends BaseResource {

	public GraphDefinitionLinkTargetCompartment() {
	}


	private static final int MAX_DESC_LENGTH = 4096;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "GRAPH_COMPARTMENT_ID")
	private Long graphDefinitionCompartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "GRAPH_LINK_TARGET_ID",foreignKey= @ForeignKey(name="FK_GRAPH_LINK_TARGET_COMPARTMENT_ID"))
	private GraphDefinitionLinkTarget graphDefinitionLinkTarget;


	@Column(name="COMP_USE")
	String use;

	@Enumerated(EnumType.ORDINAL)
	@Column(name= "COMP_CODE")
	private GraphDefinition.CompartmentCode code;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "COMP_RULE")
	private GraphDefinition.GraphCompartmentRule rule;

	@Column(name="DESCRIPTION",length = MAX_DESC_LENGTH,nullable = true)
	private String description;

	@Column(name="EXPRESSION",length = MAX_DESC_LENGTH,nullable = true)
	private String expression;


	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}


	@Override
	public Long getId() {
		return null;
	}

	public Long getGraphDefinitionCompartment() {
		return graphDefinitionCompartment;
	}

	public void setGraphDefinitionCompartment(Long graphDefinitionCompartment) {
		this.graphDefinitionCompartment = graphDefinitionCompartment;
	}

	public GraphDefinitionLinkTarget getGraphDefinitionLinkTarget() {
		return graphDefinitionLinkTarget;
	}

	public void setGraphDefinitionLinkTarget(GraphDefinitionLinkTarget graphDefinitionLinkTarget) {
		this.graphDefinitionLinkTarget = graphDefinitionLinkTarget;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public GraphDefinition.CompartmentCode getCode() {
		return code;
	}

	public void setCode(GraphDefinition.CompartmentCode code) {
		this.code = code;
	}

	public GraphDefinition.GraphCompartmentRule getRule() {
		return rule;
	}

	public void setRule(GraphDefinition.GraphCompartmentRule rule) {
		this.rule = rule;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
