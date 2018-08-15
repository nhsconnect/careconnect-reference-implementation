package uk.nhs.careconnect.ri.entity.goal;

import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.RiskAssessment;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.medicationStatement.MedicationStatementEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.entity.riskAssessment.RiskAssessmentEntity;

import javax.persistence.*;

@Entity
@Table(name="GoalAddresses", uniqueConstraints= @UniqueConstraint(name="PK_GOAL_ADDRESSES", columnNames={"GOAL_ADDRESSES_ID"})
		,indexes = {}
		)
public class GoalAddresses extends BaseResource {

	public GoalAddresses() 
	{
	}
    public GoalAddresses(GoalEntity goal) {
		this.goal = goal;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "GOAL_ADDRESSES_ID")
    private Long addressesId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "GOAL_ID")
	private GoalEntity goal;

	public Long getAddressesId() { return addressesId; }
	public void setAddressesId(Long addressesId) { this.addressesId = addressesId; }

	public GoalEntity getGoal() {
		return goal;
	}

	public void setGoal(GoalEntity goal) {
		this.goal = goal;
	}

	@Override
	public Long getId() {
		return this.addressesId;
	}


}
