package uk.gov.hscic.medication.orders;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "medication_orders")
public class MedicationOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_written")
    private Date dateWritten;
    
    @Column(name = "order_status")
    private String orderStatus;
    
    @Column(name = "patient_id")
    private Long patientId;
    
    @Column(name = "author_id")
    private Long autherId;
    
    @Column(name = "medication_id")
    private Long medicationId;
    
    @Column(name = "dosage_text")
    private String dosageText;
    
    @Column(name = "dispense_quantity_text")
    private String dispenseQuantityText;
    
    @Column(name = "dispense_review_date")
    private Date dispenseReviewDate;
    
    @Column(name = "dispense_medication_id")
    private Long dispenseMedicationId;
    
    @Column(name = "dispense_repeats_allowed")
    private int dispenseRepeatsAllowed;

    @Column(name = "lastUpdated")
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
