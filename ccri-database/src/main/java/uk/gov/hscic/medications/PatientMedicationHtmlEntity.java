package uk.gov.hscic.medications;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "medications_html")
public class PatientMedicationHtmlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nhsNumber")
    private String nhsNumber;

    @Column(name = "currentRepeatPast")
    private String currentRepeatPast;

    @Column(name = "startDate")
    private String startDate;

    @Column(name = "medicationItem")
    private String medicationItem;

    @Column(name = "scheduledEnd")
    private String scheduledEnd;

    @Column(name = "daysDuration")
    private String daysDuration;

    @Column(name = "details")
    private String details;

    @Column(name = "lastIssued")
    private String lastIssued;

    @Column(name = "reviewDate")
    private String reviewDate;

    @Column(name = "numberIssued")
    private String numberIssued;

    @Column(name = "maxIssues")
    private String maxIssues;

    @Column(name = "typeMed")
    private String typeMed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public String getCurrentRepeatPast() {
        return currentRepeatPast;
    }

    public void setCurrentRepeatPast(String currentRepeatPast) {
        this.currentRepeatPast = currentRepeatPast;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getMedicationItem() {
        return medicationItem;
    }

    public void setMedicationItem(String medicationItem) {
        this.medicationItem = medicationItem;
    }

    public String getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(String scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public String getDaysDuration() {
        return daysDuration;
    }

    public void setDaysDuration(String daysDuration) {
        this.daysDuration = daysDuration;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLastIssued() {
        return lastIssued;
    }

    public void setLastIssued(String lastIssued) {
        this.lastIssued = lastIssued;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getNumberIssued() {
        return numberIssued;
    }

    public void setNumberIssued(String numberIssued) {
        this.numberIssued = numberIssued;
    }

    public String getMaxIssues() {
        return maxIssues;
    }

    public void setMaxIssues(String maxIssues) {
        this.maxIssues = maxIssues;
    }

    public String getTypeMed() {
        return typeMed;
    }

    public void setTypeMed(String typeMed) {
        this.typeMed = typeMed;
    }


}
