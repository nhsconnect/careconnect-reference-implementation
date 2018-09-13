package uk.nhs.careconnect.ri.database.entity.medicationStatement;


import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;

import javax.persistence.*;


@Entity
@Table(name="MedicationStatementBasedOn", uniqueConstraints= @UniqueConstraint(name="PK_MEDICATION_STATEMENT_BASEDON", columnNames={"MEDICATION_STATEMENT_BASEDON_ID"})
		)
public class MedicationStatementBasedOn {

	public MedicationStatementBasedOn() {

	}

	public MedicationStatementBasedOn(MedicationStatementEntity statement) {
		this.statement = statement;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MEDICATION_STATEMENT_BASEDON_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MEDICATION_STATEMENT_ID",foreignKey= @ForeignKey(name="FK_MEDICATION_STATEMENT_MEDICATION_STATEMENT_BASEDON"))
	private MedicationStatementEntity statement;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRESCRIPTION_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_MEDICATION_STATEMENT"))

	private MedicationRequestEntity prescription;


	public Long getBasedOnId() { return identifierId; }
	public void setBasedOnId(Long identifierId) { this.identifierId = identifierId; }

	public MedicationStatementEntity getMedicationStatement() {
	        return this.statement;
	}

	public void setMedicationStatement(MedicationStatementEntity statement) {
	        this.statement = statement;
	}

	public Long getIdentifierId() {
		return identifierId;
	}

	public void setIdentifierId(Long identifierId) {
		this.identifierId = identifierId;
	}

	public MedicationStatementEntity getStatement() {
		return statement;
	}

	public void setStatement(MedicationStatementEntity statement) {
		this.statement = statement;
	}

	public MedicationRequestEntity getPrescription() {
		return prescription;
	}

	public void setPrescription(MedicationRequestEntity prescription) {
		this.prescription = prescription;
	}
}
