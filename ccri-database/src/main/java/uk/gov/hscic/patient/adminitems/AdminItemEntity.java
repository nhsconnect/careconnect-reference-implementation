package uk.gov.hscic.patient.adminitems;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "adminitems")
public class AdminItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nhsNumber")
    private String nhsNumber;

    @Column(name = "sectionDate")
    private Date sectionDate;

    @Column(name = "adminDate")
    private String adminDate;

    @Column(name = "entry")
    private String entry;

    @Column(name = "details")
    private String details;

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

    public String getAdminDate() {
        return adminDate;
    }

    public void setAdminDate(String adminDate) {
        this.adminDate = adminDate;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
