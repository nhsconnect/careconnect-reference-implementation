package uk.nhs.careconnect.ri.database.entity.observation;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ObservationCategory", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_CATEGORY", columnNames={"OBSERVATION_CATEGORY_ID"})
        ,indexes = { @Index(name="IDX_OBSERVATION_CATEGORY", columnList = "category")}
)
public class ObservationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "OBSERVATION_CATEGORY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_CATEGORY_OBSERVATION_ID"))
    private ObservationEntity observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category",foreignKey= @ForeignKey(name="FK_OBSERVATION_CATEGORY_CATEGORY_CONCEPT_ID"))
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
