package uk.nhs.careconnect.ri.entity.healthcareService;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerRole;

import javax.persistence.*;

@Entity
@Table(name = "HealthcareServiceLocation")
public class HealthcareServiceLocation extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_LOCATION_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_LOCATION_SERVICE_ID"))
    private HealthcareServiceEntity service;

    @ManyToOne
    @JoinColumn(name="LOCATION_ID",foreignKey= @ForeignKey(name="FK_SERVICE_LOCATION_LOCATION_ID"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private LocationEntity location;

    public Long getId()
    {
        return this.myId;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public HealthcareServiceEntity setLocation(LocationEntity location) {
        this.location = location;
        return this.service;
    }

    public HealthcareServiceEntity getPractitionerRole() {
        return service;
    }

    public HealthcareServiceLocation setPractitionerRole(HealthcareServiceEntity service) {
        this.service = service;
        return this;
    }


}
