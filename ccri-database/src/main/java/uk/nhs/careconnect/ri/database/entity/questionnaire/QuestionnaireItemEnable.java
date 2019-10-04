package uk.nhs.careconnect.ri.database.entity.questionnaire;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="QuestionnaireItemEnable", uniqueConstraints= @UniqueConstraint(name="PK_QUESTIONNAIRE_ITEM_ENABLE", columnNames={"QUESTIONNAIRE_ITEM_ENABLE_ID"})
		,indexes = {}
		)
public class QuestionnaireItemEnable extends BaseResource {

	public QuestionnaireItemEnable() {
	}
    public QuestionnaireItemEnable(QuestionnaireItem questionnaireItem) {
		this.questionnaireItem = questionnaireItem;
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "QUESTIONNAIRE_ITEM_ENABLE_ID")
    private Long itemEnableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "QUESTIONNAIRE_ITEM_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_ENABLE_QUESTIONNAIRE_ID"))
	private QuestionnaireItem questionnaireItem;

	@Column(name="question")
	String question;

	@Column(name="hasAnswer")
	Boolean hasAnswer;

	@Column(name="answerString")
	String answerString;

	@Column(name="answerInteger")
	String answerInteger;

	@Column(name="answerDateTime")
	String answerDateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="VALUE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_ITEM_ENABLE_VALUE_CONCEPT_ID"))
	private ConceptEntity answerCode;

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public Long getItemEnableId() {
		return itemEnableId;
	}

	public void setItemEnableId(Long itemEnableId) {
		this.itemEnableId = itemEnableId;
	}

	public QuestionnaireItem getQuestionnaireItem() {
		return questionnaireItem;
	}

	public void setQuestionnaireItem(QuestionnaireItem questionnaireItem) {
		this.questionnaireItem = questionnaireItem;
	}

	public String getAnswerString() {
		return answerString;
	}

	public void setAnswerString(String answerString) {
		this.answerString = answerString;
	}

	public String getAnswerInteger() {
		return answerInteger;
	}

	public void setAnswerInteger(String answerInteger) {
		this.answerInteger = answerInteger;
	}

	public String getAnswerDateTime() {
		return answerDateTime;
	}

	public void setAnswerDateTime(String answerDateTime) {
		this.answerDateTime = answerDateTime;
	}

	public ConceptEntity getAnswerCode() {
		return answerCode;
	}

	public void setAnswerCode(ConceptEntity answerCode) {
		this.answerCode = answerCode;
	}


	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public Boolean getHasAnswer() {
		return hasAnswer;
	}

	public void setHasAnswer(Boolean hasAnswer) {
		this.hasAnswer = hasAnswer;
	}
}
