package uk.nhs.careconnect.ri.database.entity.medicationStatement;


import javax.persistence.*;


@Entity
@Table(name="MedicationStatementDerivedFrom", uniqueConstraints= @UniqueConstraint(name="PK_MEDICATION_STATEMENT_DERIVEDFROM", columnNames={"MEDICATION_STATEMENT_DERIVEDFROM_ID"})
		)
public class MedicationStatementDerivedFrom {

	public MedicationStatementDerivedFrom() {

	}

	public MedicationStatementDerivedFrom(MedicationStatementEntity statement) {
		this.statement = statement;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MEDICATION_STATEMENT_DERIVEDFROM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MEDICATION_STATEMENT_ID",foreignKey= @ForeignKey(name="FK_MEDICATION_STATEMENT_MEDICATION_STATEMENT_DERIVEDFROM"))
	private MedicationStatementEntity statement;

	// TO BE IMPLEMENTED

    public Long getDerivedFromId() { return identifierId; }
	public void setDerivedFromId(Long identifierId) { this.identifierId = identifierId; }

	public MedicationStatementEntity getMedicationStatement() {
	        return this.statement;
	}

	public void setMedicationStatement(MedicationStatementEntity statement) {
	        this.statement = statement;
	}




}
