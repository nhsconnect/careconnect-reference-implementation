package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ConsentCategory", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_CATEGORY", columnNames={"CONSENT_CATEGORY_ID"})
        ,indexes = { @Index(name="IDX_CONSENT_CATEGORY", columnList = "category")}
)
public class ConsentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONSENT_CATEGORY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_CATEGORY_CONSENT_ID"))
    private ConsentEntity consent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category",foreignKey= @ForeignKey(name="FK_CONSENT_CATEGORY_CATEGORY_CONCEPT_ID"))
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

    public ConsentEntity getConsent() {
        return consent;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }
    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }
}
