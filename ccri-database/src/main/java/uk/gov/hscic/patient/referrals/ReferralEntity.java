package uk.gov.hscic.patient.referrals;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "referrals")
public class ReferralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nhsNumber")
    private Long nhsNumber;

    @Column(name = "sectionDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sectionDate;

    @Column(name = "referral_from")
    private String from;

    @Column(name = "referral_to")
    private String to;

    @Column(name = "priority")
    private String priority;

    @Column(name = "details")
    private String details;

    @Column(name = "lastUpdated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(Long nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public Date getSectionDate() {
        return sectionDate;
    }

    public void setSectionDate(Date sectionDate) {
        this.sectionDate = sectionDate;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
