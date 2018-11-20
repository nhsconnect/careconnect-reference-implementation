package uk.nhs.careconnect.ri.database.entity.medicationAdministration;


import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;

import javax.persistence.*;


@Entity
@Table(name="MedicationAdministrationPartOf", uniqueConstraints= @UniqueConstraint(name="PK_MEDICATION_ADMINISTRATION_PARTOF", columnNames={"MEDICATION_ADMINISTRATION_PARTOF_ID"})
		)
public class MedicationAdministrationPartOf {

	public MedicationAdministrationPartOf() {

	}

	public MedicationAdministrationPartOf(MedicationAdministrationEntity administration) {
		this.administration = administration;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MEDICATION_ADMINISTRATION_PARTOF_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MEDICATION_ADMINISTRATION_ID",foreignKey= @ForeignKey(name="FK_MEDICATION_ADMINISTRATION_MEDICATION_ADMINISTRATION_PARTOF"))
	private MedicationAdministrationEntity administration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_MEDICATION_ADMINISTRATION"))
	private ObservationEntity observation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PROCEDURE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_MEDICATION_ADMINISTRATION"))
	private ProcedureEntity procedure;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PARTOF_ADMINISTRATION_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_MEDICATION_ADMINISTRATION"))
	private MedicationAdministrationEntity administrationPartOf;;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PARTOF_DISPENSE_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_MEDICATION_DISPENSE"))
	private MedicationDispenseEntity dispensePartOf;;

	public Long getPartOfId() { return identifierId; }
	public void setPartOfId(Long identifierId) { this.identifierId = identifierId; }

	public MedicationAdministrationEntity getMedicationAdministration() {
	        return this.administration;
	}

	public void setMedicationAdministration(MedicationAdministrationEntity administration) {
	        this.administration = administration;
	}

	public Long getIdentifierId() {
		return identifierId;
	}

	public void setIdentifierId(Long identifierId) {
		this.identifierId = identifierId;
	}

	public MedicationAdministrationEntity getAdministration() {
		return administration;
	}

	public void setAdministration(MedicationAdministrationEntity administration) {
		this.administration = administration;
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

	public MedicationAdministrationEntity getAdministrationPartOf() {
		return administrationPartOf;
	}

	public void setAdministrationPartOf(MedicationAdministrationEntity administrationPartOf) {
		this.administrationPartOf = administrationPartOf;
	}
}
