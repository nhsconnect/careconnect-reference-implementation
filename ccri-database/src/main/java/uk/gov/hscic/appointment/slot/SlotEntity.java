package uk.gov.hscic.appointment.slot;

import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.gov.hscic.appointment.appointment.AppointmentEntity;

@Entity
@Table(name = "appointment_slots")
@Cacheable(false)
public class SlotEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=true)
    @JoinColumn(name="appointmentId", referencedColumnName="id")
    private AppointmentEntity appointmentId;
    
    @Column(name = "typeCode")
    private Long typeCode;
    
    @Column(name = "typeDisplay")
    private String typeDisply;
    
    @Column(name = "scheduleReference")
    private Long scheduleReference;
    
    @Column(name = "freeBusyType")
    private String freeBusyType;
    
    @Column(name = "startDateTime")
    private Date startDateTime;
    
    @Column(name = "endDateTime")
    private Date endDateTime;
    
    @Column(name = "lastUpdated")
    private Date lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Long typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeDisply() {
        return typeDisply;
    }

    public void setTypeDisply(String typeDisply) {
        this.typeDisply = typeDisply;
    }

    public Long getScheduleReference() {
        return scheduleReference;
    }

    public void setScheduleReference(Long scheduleReference) {
        this.scheduleReference = scheduleReference;
    }

    public String getFreeBusyType() {
        return freeBusyType;
    }

    public void setFreeBusyType(String freeBusyType) {
        this.freeBusyType = freeBusyType;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public AppointmentEntity getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(AppointmentEntity appointmentId) {
        this.appointmentId = appointmentId;
    }
}
