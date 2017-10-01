package uk.nhs.careconnect.ri.entity.observation;

import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ObservationCategory", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_CATEGORY", columnNames={"OBSERVATION_CATEGORY_ID"})
        ,indexes = {}
)
public class ObservationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "OBSERVATION_CATEGORY_ID")
    private Long Id;

    @ManyToOne
    @JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_OBSERVATION_CATEGORY"))
    private ObservationEntity observation;

    @ManyToOne
    @JoinColumn(name="category")
    private ConceptEntity category;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getCategory() {
        return category;
    }

    public ObservationEntity getObservation() {
        return observation;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }
    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }
}
