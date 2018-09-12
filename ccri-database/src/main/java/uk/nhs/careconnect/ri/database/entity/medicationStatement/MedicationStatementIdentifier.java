package uk.nhs.careconnect.ri.database.entity.medicationStatement;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="MedicationStatementIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_MEDICATION_STATEMENT_IDENTIFIER", columnNames={"MEDICATION_STATEMENT_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_MEDICATION_STATEMENT_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class MedicationStatementIdentifier extends BaseIdentifier {

	public MedicationStatementIdentifier() {

	}

	public MedicationStatementIdentifier(MedicationStatementEntity statement) {
		this.statement = statement;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MEDICATION_STATEMENT_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MEDICATION_STATEMENT_ID",foreignKey= @ForeignKey(name="FK_MEDICATION_STATEMENT_MEDICATION_STATEMENT_IDENTIFIER"))
	private MedicationStatementEntity statement;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public MedicationStatementEntity getMedicationStatement() {
	        return this.statement;
	}

	public void setMedicationStatement(MedicationStatementEntity statement) {
	        this.statement = statement;
	}




}
