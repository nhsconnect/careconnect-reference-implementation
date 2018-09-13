package uk.nhs.careconnect.ri.database.entity.questionnaire;



import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;


import javax.persistence.*;

@Entity
@Table(name="QuestionnaireItemOptions", uniqueConstraints= @UniqueConstraint(name="PK_QUESTIONNAIRE_ITEM_OPTION", columnNames={"QUESTIONNAIRE_ITEM_OPTION_ID"})
		,indexes = {}
		)
public class QuestionnaireItemOptions extends BaseResource {

	public QuestionnaireItemOptions() {
	}
    public QuestionnaireItemOptions(QuestionnaireItem questionnaireItem) {
		this.questionnaireItem = questionnaireItem;
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "QUESTIONNAIRE_ITEM_OPTION_ID")
    private Long itemOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "QUESTIONNAIRE_ITEM_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_OPTION_QUESTIONNAIRE_ID"))
	private QuestionnaireItem questionnaireItem;

	@Column(name="valueString")
	String valueString;

	@Column(name="valueInteger")
	String valueInteger;

	@Column(name="valueDateTime")
	String valueDateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="VALUE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_ITEM_OPTION_VALUE_CONCEPT_ID"))
	private ConceptEntity valueCode;

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public Long getItemOptionId() {
		return itemOptionId;
	}

	public void setItemOptionId(Long itemOptionId) {
		this.itemOptionId = itemOptionId;
	}

	public QuestionnaireItem getQuestionnaireItem() {
		return questionnaireItem;
	}

	public void setQuestionnaireItem(QuestionnaireItem questionnaireItem) {
		this.questionnaireItem = questionnaireItem;
	}

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	public String getValueInteger() {
		return valueInteger;
	}

	public void setValueInteger(String valueInteger) {
		this.valueInteger = valueInteger;
	}

	public String getValueDateTime() {
		return valueDateTime;
	}

	public void setValueDateTime(String valueDateTime) {
		this.valueDateTime = valueDateTime;
	}

	public ConceptEntity getValueCode() {
		return valueCode;
	}

	public void setValueCode(ConceptEntity valueCode) {
		this.valueCode = valueCode;
	}
}
