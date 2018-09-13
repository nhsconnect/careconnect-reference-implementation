package uk.nhs.careconnect.ri.database.entity.carePlan;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="CarePlanCategory", uniqueConstraints= @UniqueConstraint(name="PK_CAREPLAN_CATEGORY", columnNames={"CAREPLAN_CATEGORY_ID"})
        ,indexes = { @Index(name="IDX_CAREPLAN_CATEGORY", columnList = "category")}
)
public class CarePlanCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CAREPLAN_CATEGORY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CAREPLAN_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_CATEGORY_CAREPLAN_ID"))
    private CarePlanEntity carePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category",foreignKey= @ForeignKey(name="FK_CAREPLAN_CATEGORY_CATEGORY_CONCEPT_ID"))
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

    public CarePlanEntity getCarePlan() {
        return carePlan;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }
    public void setCarePlan(CarePlanEntity carePlan) {
        this.carePlan = carePlan;
    }
}
