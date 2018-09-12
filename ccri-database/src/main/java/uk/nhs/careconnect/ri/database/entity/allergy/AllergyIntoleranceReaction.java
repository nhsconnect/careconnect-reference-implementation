package uk.nhs.careconnect.ri.database.entity.allergy;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="AllergyIntoleranceReaction", uniqueConstraints= @UniqueConstraint(name="PK_ALLERGY_REACTION", columnNames={"ALLERGY_REACTION_ID"})
        ,indexes = { @Index(name="IDX_ALLERGY_REACTION", columnList = "SUBSTANCE_CONCEPT_ID")}
)
public class AllergyIntoleranceReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ALLERGY_REACTION_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ALLERGY_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_ALLERGY_REACTION"))
    private AllergyIntoleranceEntity allergy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SUBSTANCE_CONCEPT_ID")
    private ConceptEntity substance;

    @Column(name="note",length = 5000)
    private String note;


    @Column(name="description",length = 5000)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "onset")
    private Date onsetDateTime;

    @Enumerated(EnumType.ORDINAL)
    @JoinColumn(name="severity")
    private AllergyIntolerance.AllergyIntoleranceSeverity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EXPOSURE_ROUTE_CONCEPT_ID")
    private ConceptEntity exposureRoute;

    @OneToMany(mappedBy="allergyReaction", targetEntity=AllergyIntoleranceManifestation.class)

    private List<AllergyIntoleranceManifestation> manifestations = new ArrayList<>();


    public AllergyIntoleranceReaction setId(Long id) {
        Id = id;
        return this;
    }

    public Long getId() {
        return Id;
    }

    public AllergyIntoleranceEntity getAllergy() {
        return allergy;
    }

    public AllergyIntoleranceReaction setAllergy(AllergyIntoleranceEntity allergy) {
        this.allergy = allergy;
        return this;
    }

    public AllergyIntoleranceReaction setAllergyIntolerance(AllergyIntoleranceEntity allergy) {
        this.allergy = allergy;
        return this;
    }

    public AllergyIntoleranceEntity getAllergyIntolerance() {
        return allergy;
    }

    public ConceptEntity getSubstance() {
        return substance;
    }

    public AllergyIntoleranceReaction setSubstance(ConceptEntity substance) {
        this.substance = substance;
        return this;
    }

    public String getNote() {
        return note;
    }

    public AllergyIntoleranceReaction setNote(String note) {
        this.note = note;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AllergyIntoleranceReaction setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getOnsetDateTime() {
        return onsetDateTime;
    }

    public AllergyIntoleranceReaction setOnsetDateTime(Date onsetDateTime) {
        this.onsetDateTime = onsetDateTime;
        return this;
    }

    public AllergyIntolerance.AllergyIntoleranceSeverity getSeverity() {
        return severity;
    }

    public AllergyIntoleranceReaction setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity severity) {
        this.severity = severity;
        return this;
    }

    public ConceptEntity getExposureRoute() {
        return exposureRoute;
    }

    public AllergyIntoleranceReaction setExposureRoute(ConceptEntity exposureRoute) {
        this.exposureRoute = exposureRoute;
        return this;
    }

    public List<AllergyIntoleranceManifestation> getManifestations() {
        return manifestations;
    }

    public AllergyIntoleranceReaction setManifestations(List<AllergyIntoleranceManifestation> manifestations) {
        this.manifestations = manifestations;
        return this;
    }
}
