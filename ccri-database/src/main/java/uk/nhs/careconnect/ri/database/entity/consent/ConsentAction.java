package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="ConsentAction", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_ACTION", columnNames={"CONSENT_ACTION_ID"})
        ,indexes = { @Index(name="IDX_CONSENT_ACTION", columnList = "action")}
)
public class ConsentAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONSENT_ACTION_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_ACTION_CONSENT_ID"))
    private ConsentEntity consent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="action",foreignKey= @ForeignKey(name="FK_CONSENT_ACTION_ACTION_CONCEPT_ID"))
    private ConceptEntity actionCode;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getActionCode() {
        return actionCode;
    }

    public void setActionCode(ConceptEntity actionCode) {
        this.actionCode = actionCode;
    }

    public ConsentEntity getConsent() {
        return consent;
    }


    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }
}
