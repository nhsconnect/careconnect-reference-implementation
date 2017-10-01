package uk.nhs.careconnect.ri.entity.observation;

import uk.nhs.careconnect.ri.entity.BaseIdentifier;

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

    @ManyToOne
    @JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_OBSERVATION_IDENTIFIER"))
    private ObservationEntity observation;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }




}
