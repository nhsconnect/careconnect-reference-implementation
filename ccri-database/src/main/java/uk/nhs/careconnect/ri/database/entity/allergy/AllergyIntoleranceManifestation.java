package uk.nhs.careconnect.ri.database.entity.allergy;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="AllergyIntoleranceManifestation", uniqueConstraints= @UniqueConstraint(name="PK_ALLERGY_MANIFESTATION", columnNames={"ALLERGY_MANIFESTATION_ID"})
        ,indexes = { @Index(name="IDX_ALLERGY_MANIFESTATION", columnList = "MANIFESTATION_CONCEPT_ID")}
)
public class AllergyIntoleranceManifestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ALLERGY_MANIFESTATION_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ALLERGY_REACTION_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_REACTION_ALLERGY_MANIFESTATION"))
    private AllergyIntoleranceReaction allergyReaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MANIFESTATION_CONCEPT_ID", nullable = false)
    private ConceptEntity manifestation;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public AllergyIntoleranceReaction getAllergyReactio() {
        return allergyReaction;
    }

    public AllergyIntoleranceManifestation setAllergyReaction(AllergyIntoleranceReaction allergyReaction) {
        this.allergyReaction = allergyReaction;
        return this;
    }

    public ConceptEntity getManifestation() {
        return manifestation;
    }

    public AllergyIntoleranceManifestation setManifestation(ConceptEntity manifestation) {
        this.manifestation = manifestation;
        return this;
    }
}
