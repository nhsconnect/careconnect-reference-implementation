package uk.nhs.careconnect.ri.entity.practitioner;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerHealthcareService")
public class PractitionerHealthcareService extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_SERVICE_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRACTITIONER_ROLE_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_SERVICE_PRACTITIONER_ROLE_ID"))
    private PractitionerRole practitionerRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SERVICE_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_SERVICE_SERVICE_ID"))

    private HealthcareServiceEntity service;

    public Long getId()
    {
        return this.myId;
    }

    public HealthcareServiceEntity getService() {
        return service;
    }

    public PractitionerRole setService(HealthcareServiceEntity service) {
        this.service = service;
        return this.practitionerRole;
    }

    public PractitionerRole getPractitionerRole() {
        return practitionerRole;
    }

    public PractitionerHealthcareService setPractitionerRole(PractitionerRole practitionerRole) {
        this.practitionerRole = practitionerRole;
        return this;
    }

  
}
