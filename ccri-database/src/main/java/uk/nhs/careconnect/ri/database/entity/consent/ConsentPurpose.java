package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ConsentPurpose", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_PURPOSE", columnNames={"CONSENT_PURPOSE_ID"})
        ,indexes = { @Index(name="IDX_CONSENT_PURPOSE", columnList = "category")}
)
public class ConsentPurpose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONSENT_PURPOSE_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_PURPOSE_CONSENT_ID"))
    private ConsentEntity consent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category",foreignKey= @ForeignKey(name="FK_CONSENT_PURPOSE_PURPOSE_CONCEPT_ID"))
    private ConceptEntity category;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getPurpose() {
        return category;
    }

    public ConsentEntity getConsent() {
        return consent;
    }

    public void setPurpose(ConceptEntity category) {
        this.category = category;
    }
    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }
}
