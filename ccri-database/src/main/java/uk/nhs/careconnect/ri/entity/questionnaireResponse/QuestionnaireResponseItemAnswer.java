package uk.nhs.careconnect.ri.entity.questionnaireResponse;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.entity.goal.GoalEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="QuestionnaireResponseItemAnswer", uniqueConstraints= @UniqueConstraint(name="PK_FORM_ITEM_ANSWER", columnNames={"FORM_ITEM_ANSWER_ID"})
		,indexes = {}
		)
public class QuestionnaireResponseItemAnswer extends BaseResource {

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUE_GOAL_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_ANSWER_GOAL_ID"))
	GoalEntity goal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUE_CONDITION_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_ANSWER_CONDITION_ID"))
	ConditionEntity condition;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUE_OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_ANSWER_OBSERVATION_ID"))
	ObservationEntity observation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUE_PATIENT_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_ANSWER_PATIENT_ID"))
	private PatientEntity patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUE_DOCUMENT_REFERENCE_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_ANSWER_DOCUMENT_REFERENCE_ID"))
	private DocumentReferenceEntity documentReference;

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

	public GoalEntity getGoal() {
		return goal;
	}

	public void setGoal(GoalEntity goal) {
		this.goal = goal;
	}

	public ConditionEntity getCondition() {
		return condition;
	}

	public void setCondition(ConditionEntity condition) {
		this.condition = condition;
	}

	public ObservationEntity getObservation() {
		return observation;
	}

	public void setObservation(ObservationEntity observation) {
		this.observation = observation;
	}

	public PatientEntity getPatient() {
		return patient;
	}

	public void setPatient(PatientEntity patient) {
		this.patient = patient;
	}

	public DocumentReferenceEntity getDocumentReference() {
		return documentReference;
	}

	public void setDocumentReference(DocumentReferenceEntity documentReference) {
		this.documentReference = documentReference;
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
