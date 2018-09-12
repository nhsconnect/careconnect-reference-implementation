package uk.nhs.careconnect.ri.database.entity.referral;

import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;


import javax.persistence.*;

@Entity
@Table(name="ReferralRequestRecipient", uniqueConstraints= @UniqueConstraint(name="PK_REFERRAL_RECIPIENT", columnNames={"REFERRAL_RECIPIENT_ID"})
        ,indexes = { @Index(name="IDX_REFERRAL_RECIPIENT", columnList = "REFERRAL_RECIPIENT_ID")}
)
public class ReferralRequestRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "REFERRAL_RECIPIENT_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REFERRAL_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REFERRAL_RECIPIENT"))
    private ReferralRequestEntity referral;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_RECIPIENT_PRACTITIONER"))

    private PractitionerEntity practitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ORGNANISATION_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_RECIPIENT_ORGANISATION"))

    private OrganisationEntity organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SERVICE_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_RECIPIENT_SERVICE"))

    private HealthcareServiceEntity service;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }


    public ReferralRequestEntity getReferralRequest() {
        return referral;
    }



    public ReferralRequestRecipient setReferralRequest(ReferralRequestEntity referral) {
        this.referral = referral;
        return this;
    }

    public ReferralRequestEntity getReferral() {
        return referral;
    }

    public void setReferral(ReferralRequestEntity referral) {
        this.referral = referral;
    }

    public PractitionerEntity getPractitioner() {
        return practitioner;
    }

    public void setPractitioner(PractitionerEntity practitioner) {
        this.practitioner = practitioner;
    }

    public OrganisationEntity getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationEntity organisation) {
        this.organisation = organisation;
    }

    public HealthcareServiceEntity getService() {
        return service;
    }

    public void setService(HealthcareServiceEntity service) {
        this.service = service;
    }
}
