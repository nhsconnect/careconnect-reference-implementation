package uk.nhs.careconnect.ri.database.entity.medicationStatement;


import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;

import javax.persistence.*;


@Entity
@Table(name="MedicationStatementPartOf", uniqueConstraints= @UniqueConstraint(name="PK_MEDICATION_STATEMENT_PARTOF", columnNames={"MEDICATION_STATEMENT_PARTOF_ID"})
		)
public class MedicationStatementPartOf {

	public MedicationStatementPartOf() {

	}

	public MedicationStatementPartOf(MedicationStatementEntity statement) {
		this.statement = statement;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MEDICATION_STATEMENT_PARTOF_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MEDICATION_STATEMENT_ID",foreignKey= @ForeignKey(name="FK_MEDICATION_STATEMENT_MEDICATION_STATEMENT_PARTOF"))
	private MedicationStatementEntity statement;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_MEDICATION_STATEMENT"))
	private ObservationEntity observation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PROCEDURE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_MEDICATION_STATEMENT"))
	private ProcedureEntity procedure;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PARTOF_STATEMENT_ID",foreignKey= @ForeignKey(name="FK_STATEMENT_MEDICATION_STATEMENT"))
	private MedicationStatementEntity statementPartOf;;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PARTOF_DISPENSE_ID",foreignKey= @ForeignKey(name="FK_STATEMENT_MEDICATION_DISPENSE"))
	private MedicationDispenseEntity dispensePartOf;;

	public Long getPartOfId() { return identifierId; }
	public void setPartOfId(Long identifierId) { this.identifierId = identifierId; }

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

	public ObservationEntity getObservation() {
		return observation;
	}

	public void setObservation(ObservationEntity observation) {
		this.observation = observation;
	}

	public ProcedureEntity getProcedure() {
		return procedure;
	}

	public void setProcedure(ProcedureEntity procedure) {
		this.procedure = procedure;
	}

	public MedicationStatementEntity getStatementPartOf() {
		return statementPartOf;
	}

	public void setStatementPartOf(MedicationStatementEntity statementPartOf) {
		this.statementPartOf = statementPartOf;
	}
}
