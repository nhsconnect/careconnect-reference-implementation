package uk.nhs.careconnect.ri.database.entity.encounter;

import org.hl7.fhir.dstu3.model.Encounter;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="EncounterLocation", uniqueConstraints= @UniqueConstraint(name="PK_ENCOUNTER_LOCATION", columnNames={"ENCOUNTER_LOCATION_ID"})
        ,indexes = {
        @Index(name="IDX_ENCOUNTER_LOCATION", columnList = "LOCATION_ID"),
        @Index(name="IDX_ENCOUNTER_LOCATION_ENCOUNTER_ID", columnList = "ENCOUNTER_ID")
}
)
public class EncounterLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ENCOUNTER_LOCATION_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_LOCATION_ENCOUNTER_ID"))
    private EncounterEntity encounter;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Encounter.EncounterLocationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "LOCATION_ID", nullable = false, foreignKey= @ForeignKey(name="FK_ENCOUNTER_LOCATION_LOCATION_ID"))
    private LocationEntity location;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodStartDate")
    private Date periodStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodEndDate")
    private Date periodEndDate;



    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public EncounterLocation setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
        return this;
    }

    public EncounterLocation setLocation(LocationEntity location) {
        this.location = location;
        return this;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public Encounter.EncounterLocationStatus getStatus() {
        return status;
    }

    public void setStatus(Encounter.EncounterLocationStatus status) {
        this.status = status;
    }

    public Date getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(Date periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public Date getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(Date periodEndDate) {
        this.periodEndDate = periodEndDate;
    }
}
