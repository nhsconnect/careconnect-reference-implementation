package uk.nhs.careconnect.ri.database.entity.observationDefinition;

import uk.nhs.careconnect.ri.database.entity.BaseCodeableConcept;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;

import javax.persistence.*;

@Entity
@Table(name="ObservationDefinitionCategory", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_DEF_CATEGORY", columnNames={"OBSERVATION_DEF_CATEGORY_ID"})
        ,indexes = { @Index(name="IDX_OBSERVATION_DEF_CATEGORY", columnList = "CONCEPT_CODE")}
)
public class ObservationDefinitionCategory extends BaseCodeableConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "OBSERVATION_DEF_CATEGORY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OBSERVATION_DEF_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_DEF_CATEGORY_OBSERVATION_DEF_ID"))
    private ObservationDefinitionEntity observationDefinition;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ObservationDefinitionEntity getObservationDefinition() {
        return observationDefinition;
    }

    public void setObservationDefinition(ObservationDefinitionEntity observationDefinition) {
        this.observationDefinition = observationDefinition;
    }
}
