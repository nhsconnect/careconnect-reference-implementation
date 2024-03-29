package uk.nhs.careconnect.ri.database.entity.questionnaire;

import org.hl7.fhir.dstu3.model.Questionnaire;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="QuestionnaireItem", uniqueConstraints= @UniqueConstraint(name="PK_QUESTIONNAIRE_ITEM", columnNames={"QUESTIONNAIRE_ITEM_ID"})
		,indexes = {}
		)
public class QuestionnaireItem extends BaseResource {

	public QuestionnaireItem() {
	}
    public QuestionnaireItem(QuestionnaireEntity questionnaire) {
		this.questionnaire = questionnaire;
	}

	private static final int MAX_DESC_LENGTH = 4096;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "QUESTIONNAIRE_ITEM_ID")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "QUESTIONNAIRE_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_QUESTIONNAIRE_ID"))
	private QuestionnaireEntity questionnaire;

	@Column(name="LINKID",nullable = false)
	private String linkId;



	@Column(name="PREFIX",length = MAX_DESC_LENGTH,nullable = true)
	private String prefix;

	@OneToMany(mappedBy="questionnaireItem", targetEntity=QuestionnaireItemCode.class)
	private Set<QuestionnaireItemCode> codes = new HashSet<>();

	@Column(name="ITEM_TEXT",nullable = true)
	private String itemText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ITEM_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_ITEM_CODE_CONCEPT_ID"))
	private ConceptEntity itemCode;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="ITEM_TYPE")
	private Questionnaire.QuestionnaireItemType itemType;

	@OneToMany(mappedBy="questionnaireItem", targetEntity=QuestionnaireItemEnable.class)
	private Set<QuestionnaireItemEnable> enabled = new HashSet<>();

	@Column(name="required")
	private Boolean required;

	@Column(name="repeats")
	private Boolean repeats;

	@Column(name="readOnly")
	private Boolean readOnly;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "QUESTIONNAIRE_ITEM_PARENT_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_QUESTIONNAIRE_ITEM_ID"))
	private QuestionnaireItem questionnaireParentItem;


	@Column (name = "OPTIONS_VALUESET_STR")
	private String valueSetOptions;

	@OneToMany(mappedBy="questionnaireItem", targetEntity=QuestionnaireItemOptions.class)
	private Set<QuestionnaireItemOptions> options = new HashSet<>();

	@OneToMany(mappedBy="questionnaireParentItem", targetEntity=QuestionnaireItem.class)
	@OrderBy(value = "itemId ASC")
	private Set<QuestionnaireItem> childItems = new HashSet<>();

	@Column(name="ALLOWED_PROFILE",nullable = true)
	private String allowedProfile;

	@Column(name="ALLOWED_RESOURCE",nullable = true)
	private String allowedResource;

	@Column(name = "DEFINITION", length = MAX_DESC_LENGTH)
	private String designNote;

	@Column(name = "DEFINITION_URI", length = MAX_DESC_LENGTH)
	private String definitionUri;

	public String getAllowedProfile() {
		return allowedProfile;
	}

	public void setAllowedProfile(String allowedProfile) {
		this.allowedProfile = allowedProfile;
	}

	public String getAllowedResource() {
		return allowedResource;
	}

	public void setAllowedResource(String allowedResource) {
		this.allowedResource = allowedResource;
	}

	public Long getItemId() { return itemId; }
	public void setItemId(Long itemId) { this.itemId = itemId; }

	public QuestionnaireEntity getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(QuestionnaireEntity questionnaire) {
		this.questionnaire = questionnaire;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public QuestionnaireEntity getForm() {
		return questionnaire;
	}

	public void setForm(QuestionnaireEntity questionnaire) {
		this.questionnaire = questionnaire;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public ConceptEntity getItemCode() {
		return itemCode;
	}

	public void setItemCode(ConceptEntity itemCode) {
		this.itemCode = itemCode;
	}

	public Questionnaire.QuestionnaireItemType getItemType() {
		return itemType;
	}

	public void setItemType(Questionnaire.QuestionnaireItemType itemType) {
		this.itemType = itemType;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getRepeats() {
		return repeats;
	}

	public void setRepeats(Boolean repeats) {
		this.repeats = repeats;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public QuestionnaireItem getQuestionnaireParentItem() {
		return questionnaireParentItem;
	}

	public void setQuestionnaireParentItem(QuestionnaireItem questionnaireParentItem) {
		this.questionnaireParentItem = questionnaireParentItem;
	}

	public String getValueSetOptions() {
		return valueSetOptions;
	}

	public void setValueSetOptions(String valueSetOptions) {
		this.valueSetOptions = valueSetOptions;
	}

	public Set<QuestionnaireItemOptions> getOptions() {
		return options;
	}

	public void setOptions(Set<QuestionnaireItemOptions> options) {
		this.options = options;
	}

	public String getItemText() {
		return itemText;
	}

	public void setItemText(String itemText) {
		this.itemText = itemText;
	}

	@Override
	public Long getId() {
		return null;
	}

	public Set<QuestionnaireItem> getChildItems() {
		return childItems;
	}

	public void setChildItems(Set<QuestionnaireItem> childItems) {
		this.childItems = childItems;
	}

	public String getDesignNote() {
		return designNote;
	}

	public void setDesignNote(String designNote) {
		this.designNote = designNote;
	}

	public String getDefinitionUri() {
		return definitionUri;
	}

	public void setDefinitionUri(String definitionUri) {
		this.definitionUri = definitionUri;
	}

	public Set<QuestionnaireItemEnable> getEnabled() {
		return enabled;
	}

	public void setEnabled(Set<QuestionnaireItemEnable> enabled) {
		this.enabled = enabled;
	}

	public Set<QuestionnaireItemCode> getCodes() {
		return codes;
	}

	public void setCodes(Set<QuestionnaireItemCode> codes) {
		this.codes = codes;
	}
}
