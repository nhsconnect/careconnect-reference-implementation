package uk.nhs.careconnect.ri.database.entity.questionnaireResponse;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="QuestionnaireResponseItem", uniqueConstraints= @UniqueConstraint(name="PK_FORM_ITEM", columnNames={"FORM_ITEM_ID"})
		,indexes = {}
		)
public class QuestionnaireResponseItem extends BaseResource {

	public QuestionnaireResponseItem() {
	}
    public QuestionnaireResponseItem(QuestionnaireResponseEntity form) {
		this.form = form;
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "FORM_ITEM_ID")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FORM_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_FORM_ID"))
	private QuestionnaireResponseEntity form;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_PATIENT_ID"))
	private PatientEntity patient;

	@Column(name="LINKID",nullable = false)
	private String linkId;

    @Column(name="DEFINITION",nullable = true)
    private String definition;

	@Column(name="TEXT",length = MAX_DESC_LENGTH,nullable = true)
	private String text;

	@OneToMany(mappedBy="item", targetEntity=QuestionnaireResponseItemAnswer.class)
	private Set<QuestionnaireResponseItemAnswer> answers = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PARENT_ITEM_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_PARENT_ITEM_ID"))
	private QuestionnaireResponseItem parentItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PARENT_ANSWER_ID",foreignKey= @ForeignKey(name="FK_FORM_ITEM_PARENT_ANSWER_ID"))
	private QuestionnaireResponseItemAnswer parentAnswer;

	@OneToMany(mappedBy="parentItem", targetEntity=QuestionnaireResponseItem.class)
	private Set<QuestionnaireResponseItem> items = new HashSet<>();

	public Long getItemId() { return itemId; }
	public void setItemId(Long itemId) { this.itemId = itemId; }

	public QuestionnaireResponseEntity getQuestionnaireResponse() {
		return form;
	}

	public void setQuestionnaireResponse(QuestionnaireResponseEntity form) {
		this.form = form;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public QuestionnaireResponseEntity getForm() {
		return form;
	}

	public void setForm(QuestionnaireResponseEntity form) {
		this.form = form;
	}

	public PatientEntity getPatient() {
		return patient;
	}

	public void setPatient(PatientEntity patient) {
		this.patient = patient;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Set<QuestionnaireResponseItemAnswer> getAnswers() {
		return answers;
	}

	public void setAnswers(Set<QuestionnaireResponseItemAnswer> answers) {
		this.answers = answers;
	}

	@Override
	public Long getId() {
		return null;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public QuestionnaireResponseItem getParentItem() {
		return parentItem;
	}

	public void setParentItem(QuestionnaireResponseItem parentItem) {
		this.parentItem = parentItem;
	}

	public QuestionnaireResponseItemAnswer getParentAnswer() {
		return parentAnswer;
	}

	public void setParentAnswer(QuestionnaireResponseItemAnswer parentAnswer) {
		this.parentAnswer = parentAnswer;
	}

	public Set<QuestionnaireResponseItem> getItems() {
		return items;
	}

	public void setItems(Set<QuestionnaireResponseItem> items) {
		this.items = items;
	}

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
