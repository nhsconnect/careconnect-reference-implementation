package uk.nhs.careconnect.ri.database.entity.questionnaire;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="QuestionnaireItemCode", uniqueConstraints= @UniqueConstraint(name="PK_QUESTIONNAIRE_ITEM_CODE", columnNames={"QUESTIONNAIRE_ITEM_CODE_ID"})
		,indexes = {}
		)
public class QuestionnaireItemCode extends BaseResource {

	public QuestionnaireItemCode() {
	}
    public QuestionnaireItemCode(QuestionnaireItem questionnaireItem) {
		this.questionnaireItem = questionnaireItem;
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "QUESTIONNAIRE_ITEM_CODE_ID")
    private Long itemCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "QUESTIONNAIRE_ITEM_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_CODE_QUESTIONNAIRE_ID"))
	private QuestionnaireItem questionnaireItem;



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_ITEM_CODE_VALUE_CONCEPT_ID"))
	private ConceptEntity code;

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public Long getItemCodeId() {
		return itemCodeId;
	}

	public void setItemCodeId(Long itemCodeId) {
		this.itemCodeId = itemCodeId;
	}

	public QuestionnaireItem getQuestionnaireItem() {
		return questionnaireItem;
	}

	public void setQuestionnaireItem(QuestionnaireItem questionnaireItem) {
		this.questionnaireItem = questionnaireItem;
	}

	public ConceptEntity getCode() {
		return code;
	}

	public void setCode(ConceptEntity code) {
		this.code = code;
	}
}
