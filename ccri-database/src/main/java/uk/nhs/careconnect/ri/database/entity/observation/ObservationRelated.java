package uk.nhs.careconnect.ri.database.entity.observation;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;

import javax.persistence.*;

@Entity
@Table(name="ObservationRelated", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_RELATED", columnNames={"OBSERVATION_RELATED_ID"})
)
public class ObservationRelated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "OBSERVATION_RELATED_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_RELATED_OBSERVATION_ID"))
    private ObservationEntity observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="RELATED_OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_RELATED_RELATED_OBSERVATION_ID"))
    private ObservationEntity relatedObservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="RELATED_FORM_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_RELATED_RELATED_FORM_ID"))
    private QuestionnaireResponseEntity
            relatedForm;

    // The parent should not be null but child observations don't have a status.
    @Enumerated(EnumType.ORDINAL)
    @Column(name="type")
    private Observation.ObservationRelationshipType type;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ObservationEntity getObservation() {
        return observation;
    }

    public ObservationEntity getRelatedObservation() {
        return relatedObservation;
    }

    public void setRelatedObservation(ObservationEntity relatedObservation) {
        this.relatedObservation = relatedObservation;
    }

    public QuestionnaireResponseEntity getRelatedForm() {
        return relatedForm;
    }

    public void setRelatedForm(QuestionnaireResponseEntity relatedForm) {
        this.relatedForm = relatedForm;
    }

    public Observation.ObservationRelationshipType getType() {
        return type;
    }

    public void setType(Observation.ObservationRelationshipType type) {
        this.type = type;
    }

    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }
}
