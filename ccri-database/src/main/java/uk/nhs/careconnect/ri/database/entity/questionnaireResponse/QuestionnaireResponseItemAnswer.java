package uk.nhs.careconnect.ri.database.entity.questionnaireResponse;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="QuestionnaireResponseItemAnswer", uniqueConstraints= @UniqueConstraint(name="PK_FORM_ITEM_ANSWER", columnNames={"FORM_ITEM_ANSWER_ID"})
		,indexes = {}
		)
public class QuestionnaireResponseItemAnswer extends BaseReferenceItem {

	public QuestionnaireResponseItemAnswer() {
	}
    public QuestionnaireResponseItemAnswer(QuestionnaireResponseItem item) {
		this.item = item;
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "FORM_ITEM_ANSWER_ID")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FORM_ITEM_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_ANSWER_FORM_ITEM_ID"))
	private QuestionnaireResponseItem item;

	@Column(name="valueBoolean")
	Boolean valueBoolean;

	@Column(name="valueInteger")
	Integer valueInteger;

	@Column(name="valueString")
	String valueString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="value_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_ANSWER_CONCEPT_ID"))
    ConceptEntity valueCoding;

	@OneToMany(mappedBy="parentAnswer", targetEntity=QuestionnaireResponseItem.class)
	private Set<QuestionnaireResponseItem> items = new HashSet<>();

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public Long getAnswerId() {
		return answerId;
	}

	public void setAnswerId(Long answerId) {
		this.answerId = answerId;
	}

	public QuestionnaireResponseItem getItem() {
		return item;
	}

	public void setItem(QuestionnaireResponseItem item) {
		this.item = item;
	}

	public Boolean getValueBoolean() {
		return valueBoolean;
	}

	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	public Integer getValueInteger() {
		return valueInteger;
	}

	public void setValueInteger(Integer valueInteger) {
		this.valueInteger = valueInteger;
	}

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}


	public Set<QuestionnaireResponseItem> getItems() {
		return items;
	}

	public void setItems(Set<QuestionnaireResponseItem> items) {
		this.items = items;
	}

    public ConceptEntity getValueCoding() {
        return valueCoding;
    }

    public void setValueCoding(ConceptEntity valueCoding) {
        this.valueCoding = valueCoding;
    }
}
