package uk.nhs.careconnect.ri.database.entity.conceptMap;

import javax.persistence.*;

import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import java.util.List;


@Entity
@Table(name="ConceptMapGroup", uniqueConstraints= @UniqueConstraint(name="PK_CONCEPT_MAP_GROUP", columnNames={"CONCEPT_MAP_GROUP_ID"})
		)
public class ConceptMapGroup  {

	public ConceptMapGroup() {

	}

	public ConceptMapGroup(ConceptMapEntity conceptmapEntity) {
		this.conceptMap = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONCEPT_MAP_GROUP_ID")
	private Long groupId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONCEPT_MAP_ID",foreignKey= @ForeignKey(name="FK_CONCEPT_MAP_CONCEPT_MAP_GROUP"))
	private ConceptMapEntity conceptMap;

	@OneToMany(mappedBy="conceptMapGroup", targetEntity= ConceptMapGroupElement.class)
	private List<ConceptMapGroupElement> elements;

	@Column(name = "SOURCE")
	private String source;

	@Column(name = "SOURCE_VERSION")
	private String sourceVersion;

	@Column(name = "TARGET")
	private String target;

	@Column(name = "TARGET_VERSION")
	private String targetVersion;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="UNMAPPED_MODE_CONCEPT_ID")
	private ConceptEntity unmappedMode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="UNMAPPED_CODE_CONCEPT_ID")
	private ConceptEntity unmappedCode;

	@Column(name = "UNMAPPED_DISPLAY")
	private String unmappedDisplay;

	@Column(name = "UNMAPPED_URL")
	private String unmappedUrl;

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public ConceptMapEntity getConceptMap() {
		return conceptMap;
	}

	public void setConceptMap(ConceptMapEntity conceptMap) {
		this.conceptMap = conceptMap;
	}

	public List<ConceptMapGroupElement> getElements() {
		return elements;
	}

	public void setElements(List<ConceptMapGroupElement> elements) {
		this.elements = elements;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceVersion() {
		return sourceVersion;
	}

	public void setSourceVersion(String sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetVersion() {
		return targetVersion;
	}

	public void setTargetVersion(String targetVersion) {
		this.targetVersion = targetVersion;
	}
	
	public ConceptEntity getUnmappedMode() {
		return unmappedMode;
	}

	public void setUnmappedMode(ConceptEntity unmappedMode) {
		this.unmappedMode = unmappedMode;
	}

	public ConceptEntity getUnmappedCode() {
		return unmappedCode;
	}

	public void setUnmappedCode(ConceptEntity unmappedCode) {
		this.unmappedCode = unmappedCode;
	}

	public String getUnmappedDisplay() {
		return unmappedDisplay;
	}

	public void setUnmappedDisplay(String unmappedDisplay) {
		this.unmappedDisplay = unmappedDisplay;
	}

	public String getUnmappedUrl() {
		return unmappedUrl;
	}

	public void setUnmappedUrl(String unmappedUrl) {
		this.unmappedUrl = unmappedUrl;
	}
}
