package uk.gov.hscic.model.medication;

import java.util.Date;

public class MedicationOrderDetails {
    private Long id;
    private Date dateWritten;
    private String orderStatus;
    private Long patientId;
    private Long autherId;
    private Long medicationId;
    private String dosageText;
    private String dispenseQuantityText;
    private Date dispenseReviewDate;
    private Long dispenseMedicationId;
    private int dispenseRepeatsAllowed;
    private Date lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateWritten() {
        return dateWritten;
    }

    public void setDateWritten(Date dateWritten) {
        this.dateWritten = dateWritten;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getAutherId() {
        return autherId;
    }

    public void setAutherId(Long autherId) {
        this.autherId = autherId;
    }

    public Long getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(Long medicationId) {
        this.medicationId = medicationId;
    }

    public String getDosageText() {
        return dosageText;
    }

    public void setDosageText(String dosageText) {
        this.dosageText = dosageText;
    }

    public String getDispenseQuantityText() {
        return dispenseQuantityText;
    }

    public void setDispenseQuantityText(String dispenseQuantityText) {
        this.dispenseQuantityText = dispenseQuantityText;
    }

    public Date getDispenseReviewDate() {
        return dispenseReviewDate;
    }

    public void setDispenseReviewDate(Date dispenseReviewDate) {
        this.dispenseReviewDate = dispenseReviewDate;
    }

    public Long getDispenseMedicationId() {
        return dispenseMedicationId;
    }

    public void setDispenseMedicationId(Long dispenseMedicationId) {
        this.dispenseMedicationId = dispenseMedicationId;
    }

    public int getDispenseRepeatsAllowed() {
        return dispenseRepeatsAllowed;
    }

    public void setDispenseRepeatsAllowed(int dispenseRepeatsAllowed) {
        this.dispenseRepeatsAllowed = dispenseRepeatsAllowed;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
