package uk.nhs.careconnect.ri.database.entity.condition;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ConditionCategory", uniqueConstraints= @UniqueConstraint(name="PK_CONDITION_CATEGORY", columnNames={"CONDITION_CATEGORY_ID"})
        ,indexes = { @Index(name="IDX_CONDITION_CATEGORY", columnList = "CATEGORY_CONCEPT_ID")}
)
public class ConditionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONDITION_CATEGORY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONDITION_ID",foreignKey= @ForeignKey(name="FK_CONDITION_CONDITION_CATEGORY"))
    private ConditionEntity condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CATEGORY_CONCEPT_ID")
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

    public ConditionCategory setCategory(ConceptEntity category) {
        this.category = category;
        return this;
    }

    public ConditionCategory setCondition(ConditionEntity condition) {
        this.condition = condition;
        return this;
    }

    public ConditionEntity getCondition() {
        return condition;
    }
}
