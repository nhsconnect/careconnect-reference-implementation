package uk.nhs.careconnect.ri.database.entity.medicationAdministration;


import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;

import javax.persistence.*;


@Entity
@Table(name="MedicationAdministrationReason", uniqueConstraints= @UniqueConstraint(name="PK_MEDICATION_ADMINISTRATION_REASON", columnNames={"MEDICATION_ADMINISTRATION_REASON_ID"})
		)
public class MedicationAdministrationReason {

	public MedicationAdministrationReason() {

	}

	public MedicationAdministrationReason(MedicationAdministrationEntity administration) {
		this.administration = administration;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "MEDICATION_ADMINISTRATION_REASON_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "MEDICATION_ADMINISTRATION_ID",foreignKey= @ForeignKey(name="FK_MEDICATION_ADMINISTRATION_MEDICATION_ADMINISTRATION_REASON"))
	private MedicationAdministrationEntity administration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_MEDICATION_ADMINISTRATION_REASON"))

	private ObservationEntity observation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CONDITION_ID",foreignKey= @ForeignKey(name="FK_CONDITION_MEDICATION_ADMINISTRATION"))

	private ConditionEntity condition;

    public Long getReasonId() { return identifierId; }
	public void setReasonId(Long identifierId) { this.identifierId = identifierId; }

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

	public ConditionEntity getCondition() {
		return condition;
	}

	public void setCondition(ConditionEntity condition) {
		this.condition = condition;
	}
}
