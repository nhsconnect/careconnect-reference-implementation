package uk.nhs.careconnect.ri.database.entity.questionnaire;

import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.ResourceType;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ValueSetEntity;

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

	private static final int MAX_DESC_LENGTH = 512;

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

	@Column(name="ITEM_TEXT",nullable = true)
	private String itemText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ITEM_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_ITEM_CODE_CONCEPT_ID"))
	private ConceptEntity itemCode;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="ITEM_TYPE")
	private Questionnaire.QuestionnaireItemType itemType;

	// Use the extension to lock down References
	@Column(name="ITEM_RESOURCE_TYPE")
	private ResourceType itemReferenceType;

	@Column(name="required")
	private Boolean required;

	@Column(name="repeats")
	private Boolean repeats;

	@Column(name="readOnly")
	private Boolean readOnly;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "QUESTIONNAIRE_ITEM_PARENT_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_QUESTIONNAIRE_ITEM_ID"))
	private QuestionnaireItem questionnaireParentItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "OPTIONS_VALUESET_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_OPTIONS_VALUESET_ID"))
	private ValueSetEntity valueSetOptions;

	@OneToMany(mappedBy="questionnaireItem", targetEntity=QuestionnaireItemOptions.class)
	private Set<QuestionnaireItemOptions> options = new HashSet<>();

	@OneToMany(mappedBy="questionnaireParentItem", targetEntity=QuestionnaireItem.class)
	private Set<QuestionnaireItem> childItems = new HashSet<>();

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

	public ResourceType getItemReferenceType() {
		return itemReferenceType;
	}

	public void setItemReferenceType(ResourceType itemReferenceType) {
		this.itemReferenceType = itemReferenceType;
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

	public ValueSetEntity getValueSetOptions() {
		return valueSetOptions;
	}

	public void setValueSetOptions(ValueSetEntity valueSetOptions) {
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
}
