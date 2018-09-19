package uk.nhs.careconnect.ri.database.entity.appointment;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;

import javax.persistence.*;

@Entity
@Table(name = "AppointmentLocation")
public class AppointmentLocation extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="APPOINTMENT_LOCATION_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPOINTMENT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_LOCATION_SERVICE_ID"))
    private AppointmentEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="LOCATION_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_LOCATION_LOCATION_ID"))
    private LocationEntity location;

    @Override
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public AppointmentEntity getService() {
        return service;
    }

    public void setService(AppointmentEntity service) {
        this.service = service;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }
}
