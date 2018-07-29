package uk.nhs.careconnect.ri.entity.goal;

import org.hl7.fhir.dstu3.model.MedicationStatement;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.medicationStatement.MedicationStatementEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureEntity;

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
    @JoinColumn (name = "GOAL_ID",foreignKey= @ForeignKey(name="FK_GOAL_ADDRESSES_GOAL_ID"))
	private GoalEntity goal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_GOAL_ADDRESSES_OBSERVATION_ID"))
	private ObservationEntity observation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="STATEMENT_ID",foreignKey= @ForeignKey(name="FK_GOAL_ADDRESSES_STATEMENT_ID"))
	private MedicationStatementEntity statement;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="PROCEDURE_ID",foreignKey= @ForeignKey(name="FK_GOAL_ADDRESSES_PROCEDURE_ID"))
	private ProcedureEntity procedure;

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

    public ObservationEntity getObservation() {
        return observation;
    }

    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }

    public MedicationStatementEntity getStatement() {
        return statement;
    }

    public void setStatement(MedicationStatementEntity statement) {
        this.statement = statement;
    }

    public ProcedureEntity getProcedure() {
        return procedure;
    }

    public void setProcedure(ProcedureEntity procedure) {
        this.procedure = procedure;
    }
}
