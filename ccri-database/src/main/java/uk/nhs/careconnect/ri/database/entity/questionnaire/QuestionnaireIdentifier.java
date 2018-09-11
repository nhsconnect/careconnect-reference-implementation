package uk.nhs.careconnect.ri.database.entity.questionnaire;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="QuestionnaireIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_QUESTIONNAIRE_IDENTIFIER", columnNames={"QUESTIONNAIRE_IDENTIFIER_ID"})
		,indexes = {}
		)
public class QuestionnaireIdentifier extends BaseIdentifier {

	public QuestionnaireIdentifier() {
	}
    public QuestionnaireIdentifier(QuestionnaireEntity questionnaire) {
		this.questionnaire = questionnaire;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "QUESTIONNAIRE_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "QUESTIONNAIRE_ID",foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_IDENTIFIER_QUESTIONNAIRE_ID"))

    private QuestionnaireEntity questionnaire;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public QuestionnaireEntity getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(QuestionnaireEntity questionnaire) {
		this.questionnaire = questionnaire;
	}


}
