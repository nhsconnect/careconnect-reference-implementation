package uk.nhs.careconnect.ri.entity.questionnaire;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Questionnaire;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import javax.persistence.*;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ITEM_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_ITEM_CODE_CONCEPT_ID"))
	private ConceptEntity itemCode;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="ITEM_TYPE")
	private Questionnaire.QuestionnaireItemType itemType;


	@Column(name="required")
	private Boolean required;

	@Column(name="repeats")
	private Boolean repeats;

	@Column(name="readOnly")
	private Boolean readOnly;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "QUESTIONNAIRE_ITEM_PARENT_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_ITEM_QUESTIONNAIRE_ITEM_ID"))
	private QuestionnaireItem questionnaireParentItem;

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


	@Override
	public Long getId() {
		return null;
	}
}
