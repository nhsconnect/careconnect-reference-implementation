package uk.nhs.careconnect.ri.entity.referral;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;


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

    @ManyToOne
    @JoinColumn (name = "REFERRAL_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REFERRAL_RECIPIENT"))
    private ReferralRequestEntity referral;


    @ManyToOne
    @JoinColumn(name="PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_RECIPIENT_PRACTITIONER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PractitionerEntity practitioner;

    @ManyToOne
    @JoinColumn(name="ORGNANISATION_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_RECIPIENT_ORGANISATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private OrganisationEntity organisation;

    @ManyToOne
    @JoinColumn(name="SERVICE_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_RECIPIENT_SERVICE"))
    @LazyCollection(LazyCollectionOption.TRUE)
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

}
