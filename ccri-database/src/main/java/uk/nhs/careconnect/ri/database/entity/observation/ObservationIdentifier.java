package uk.nhs.careconnect.ri.database.entity.observation;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="ObservationIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_IDENTIFIER", columnNames={"OBSERVATION_IDENTIFIER_ID"})
		,indexes = {}
		)
public class ObservationIdentifier extends BaseIdentifier {

	public ObservationIdentifier() {
	}
    public ObservationIdentifier(ObservationEntity observation) {
		this.observation = observation;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "OBSERVATION_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_IDENTIFIER_OBSERVATION_ID"))

    private ObservationEntity observation;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ObservationEntity getObservation() {
		return observation;
	}

	public void setObservation(ObservationEntity observation) {
		this.observation = observation;
	}
}
