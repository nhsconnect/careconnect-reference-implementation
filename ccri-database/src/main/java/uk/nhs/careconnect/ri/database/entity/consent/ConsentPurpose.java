package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ConsentPurpose1")
public class ConsentPurpose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONSENT_PURPOSE_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_PURPOSE_CONSENT_ID"))
    private ConsentEntity consent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PURPOSE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_PURPOSE_PURPOSE_CONCEPT_ID"))
    private ConceptEntity purposeCode;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(ConceptEntity purposeCode) {
        this.purposeCode = purposeCode;
    }

    public ConsentEntity getConsent() {
        return consent;
    }


    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }
}
