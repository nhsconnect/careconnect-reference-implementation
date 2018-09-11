package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;

import javax.persistence.*;

@Entity
@Table(name="ConsentOrganisation", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_ORGANISATION", columnNames={"CONSENT_ORGANISATION_ID"})
        ,indexes = { @Index(name="IDX_CONSENT_ORGANISATION", columnList = "action")}
)
public class ConsentOrganisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONSENT_ORGANISATION_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_ORGANISATION_CONSENT_ID"))
    private ConsentEntity consent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="action",foreignKey= @ForeignKey(name="FK_CONSENT_ORGANISATION_ORGANISATION_CONCEPT_ID"))
    private OrganisationEntity organisation;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    
    public ConsentEntity getConsent() {
        return consent;
    }

    public OrganisationEntity getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationEntity organisation) {
        this.organisation = organisation;
    }

    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }
}
