package uk.nhs.careconnect.ri.entity.observation;

import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ObservationValueConcept", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_VALUE_CONCEPT", columnNames={"OBSERVATION_VALUE_CONCEPT_ID"})
        ,indexes = {}
)
public class ObservationValueConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "OBSERVATION_VALUE_CONCEPT_ID")
    private Long Id;

    @ManyToOne
    @JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_OBSERVATION_VALUE_CONCEPT"))
    private ObservationEntity observation;

    @ManyToOne
    @JoinColumn(name="code")
    private ConceptEntity code;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ObservationEntity getObservation() {
        return observation;
    }

    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }

    public void setCode(ConceptEntity code) {
        this.code = code;
    }

    public ConceptEntity getCode() {
        return code;
    }
}
