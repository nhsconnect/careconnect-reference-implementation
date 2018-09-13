package uk.nhs.careconnect.ri.database.entity.schedule;


import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRole;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import javax.persistence.*;

@Entity
@Table(name = "ScheduleActor")
public class ScheduleActor extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SCHEDULE_ACTOR_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ID",foreignKey= @ForeignKey(name="FK_SCHEDULE_ACTOR_SCHEDULE_ID"))
    private ScheduleEntity schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ACTOR_PRACTITIONER",foreignKey= @ForeignKey(name="FK_SCHEDULE_ACTOR_PRACTITIONER_ID"))
    private PractitionerEntity practitionerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ACTOR_PRACTITIONER_ROLE",foreignKey= @ForeignKey(name="FK_SCHEDULE_ACTOR_PRACTITIONER__ROLE_ID"))
    private PractitionerRole practitionerRole;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ACTOR_HEALTHCARE_SERVICE",foreignKey= @ForeignKey(name="FK_SCHEDULE_ACTOR_HEALTHCARE__SERVICE_ID"))
    private HealthcareServiceEntity healthcareServiceEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ACTOR_LOCATION",foreignKey= @ForeignKey(name="FK_SCHEDULE_ACTOR_LOCATION_ID"))
    private LocationEntity locationEntity;

    public Long getId() {
        return Id;
    }

    public void setMyId(Long Id) {
        this.Id = Id;
    }

    public ScheduleEntity getScheduleEntity() {
        return schedule;
    }

    public void setScheduleEntity(ScheduleEntity scheduleEntity) {
        this.schedule = scheduleEntity;
    }

    public PractitionerEntity getPractitionerEntity() {
        return practitionerEntity;
    }

    public void setPractitionerEntity(PractitionerEntity practitionerEntity) {
        this.practitionerEntity = practitionerEntity;
    }

    public PractitionerRole getPractitionerRole() {
        return practitionerRole;
    }

    public void setPractitionerRole(PractitionerRole practitionerRole) {
        this.practitionerRole = practitionerRole;
    }

    public HealthcareServiceEntity getHealthcareServiceEntity() {
        return healthcareServiceEntity;
    }

    public void setHealthcareServiceEntity(HealthcareServiceEntity healthcareServiceEntity) {
        this.healthcareServiceEntity = healthcareServiceEntity;
    }

    public LocationEntity getLocationEntity() {
        return locationEntity;
    }

    public void setLocationEntity(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
    }
}
