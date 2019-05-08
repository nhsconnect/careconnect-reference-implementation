package uk.nhs.careconnect.ri.database.entity.observationDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import javax.persistence.*;

@Entity
@Table(name="ObservationDefinitionIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_DEF_IDENTIFIER", columnNames={"OBSERVATION_DEF_IDENTIFIER_ID"})
		,indexes = {}
		)
public class ObservationDefinitionIdentifier extends BaseIdentifier {

	public ObservationDefinitionIdentifier() {
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "OBSERVATION_DEF_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OBSERVATION_DEF_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_DEF_IDENTIFIER_OBS_DEF_ID"))

    private ObservationDefinitionEntity observationDefinition;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ObservationDefinitionEntity getObservationDefinition() {
		return observationDefinition;
	}

	public void setObservationDefinition(ObservationDefinitionEntity observationDefinition) {
		this.observationDefinition = observationDefinition;
	}
}
