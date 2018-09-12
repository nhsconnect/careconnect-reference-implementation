package uk.nhs.careconnect.ri.database.entity.healthcareService;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;


import javax.persistence.*;

@Entity
@Table(name = "HealthcareServiceLocation")
public class HealthcareServiceLocation extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_LOCATION_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_LOCATION_SERVICE_ID"))
    private HealthcareServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="LOCATION_ID",foreignKey= @ForeignKey(name="FK_SERVICE_LOCATION_LOCATION_ID"))

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

    public HealthcareServiceEntity getHealthcareService() {
        return service;
    }

    public HealthcareServiceLocation setHealthcareService(HealthcareServiceEntity service) {
        this.service = service;
        return this;
    }


}
