package uk.gov.hscic.patient.clinicalitems;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "clinicalitems")
public class ClinicalItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nhsNumber")
    private String nhsNumber;

    @Column(name = "sectionDate")
    private Date sectionDate;

    @Column(name = "dateOfItem")
    private String dateOfItem;

    @Column(name = "entry")
    private String entry;

    @Column(name = "details")
    private String details;

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

    public Date getSectionDate() {
        return sectionDate;
    }

    public void setSectionDate(Date sectionDate) {
        this.sectionDate = sectionDate;
    }

    public String getDate() {
        return dateOfItem;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setDate(String date) {
        this.dateOfItem = date;
    }
}
