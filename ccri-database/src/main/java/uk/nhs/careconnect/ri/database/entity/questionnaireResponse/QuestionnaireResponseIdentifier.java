package uk.nhs.careconnect.ri.database.entity.questionnaireResponse;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="QuestionnaireResponseIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_FORM_IDENTIFIER", columnNames={"FORM_IDENTIFIER_ID"})
		,indexes = {}
		)
public class QuestionnaireResponseIdentifier extends BaseIdentifier {

	public QuestionnaireResponseIdentifier() {
	}
    public QuestionnaireResponseIdentifier(QuestionnaireResponseEntity form) {
		this.form = form;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "FORM_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FORM_ID",foreignKey= @ForeignKey(name="FK_FORM_IDENTIFIER_FORM_ID"))

    private QuestionnaireResponseEntity form;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public QuestionnaireResponseEntity getQuestionnaireResponse() {
		return form;
	}

	public void setQuestionnaireResponse(QuestionnaireResponseEntity form) {
		this.form = form;
	}


}
