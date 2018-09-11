package uk.nhs.careconnect.ri.database.entity.allergy;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;

import javax.persistence.*;

@Entity
@Table(name="AllergyIntoleranceCategory", uniqueConstraints= @UniqueConstraint(name="PK_ALLERGY_CATEGORY", columnNames={"ALLERGY_CATEGORY_ID"})
        ,indexes = { @Index(name="IDX_ALLERGY_CATEGORY", columnList = "CATEGORY_CONCEPT_ID")}
)
public class AllergyIntoleranceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ALLERGY_CATEGORY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ALLERGY_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_ALLERGY_CATEGORY"))
    private AllergyIntoleranceEntity allergy;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="CATEGORY_CONCEPT_ID")
    private AllergyIntolerance.AllergyIntoleranceCategory category;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public AllergyIntoleranceEntity getAllergy() {
        return allergy;
    }

    public void setAllergy(AllergyIntoleranceEntity allergy) {
        this.allergy = allergy;
    }

    public AllergyIntolerance.AllergyIntoleranceCategory getCategory() {
        return category;
    }

    public void setCategory(AllergyIntolerance.AllergyIntoleranceCategory category) {
        this.category = category;
    }

    public AllergyIntoleranceCategory setAllergyIntolerance(AllergyIntoleranceEntity allergy) {
        this.allergy = allergy;
        return this;
    }

    public AllergyIntoleranceEntity getAllergyIntolerance() {
        return allergy;
    }
}
