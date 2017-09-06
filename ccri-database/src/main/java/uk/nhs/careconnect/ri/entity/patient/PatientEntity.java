
package uk.nhs.careconnect.ri.entity.patient;

import uk.nhs.careconnect.ri.entity.BaseResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;


@Entity
@Table(name = "Patient")
public class PatientEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PATIENT_ID")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifiedDate", nullable = true)
    private Date updated;
    public Date getUpdatedDate() { return updated; }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdDate", nullable = true)
    private Date createdDate;
    public Date getCreatedDate() { return createdDate; }

    @Column(name = "title")
    private String title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "address_3")
    private String address3;

    @Column(name = "address_4")
    private String address4;

    @Column(name = "address_5")
    private String address5;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "phone")
    private String phone;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "nhs_number")
    private String nhsNumber;

    @Column(name = "pas_number")
    private String pasNumber;
/*
    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @ManyToOne
    @JoinColumn(name = "gp_id")
    private GPEntity gp;
*/
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastUpdated")
    private Date lastUpdated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registration_start")
    private Date registrationStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registration_end")
    private Date registrationEndDateTime;

    @Column(name = "registration_status")
    private String registrationStatus;

    @Column(name = "registration_type")
    private String registrationType;

    @Column(name = "sensitive_flag")
    private boolean sensitive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public String getAddress5() {
        return address5;
    }

    public void setAddress5(String address5) {
        this.address5 = address5;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public String getPasNumber() {
        return pasNumber;
    }

    public void setPasNumber(String pasNumber) {
        this.pasNumber = pasNumber;
    }

    /*
   // public DepartmentEntity getDepartment() {
        return department;
    }

  //  public void setDepartment(DepartmentEntity department) {
        this.department = department;
    }

   // public GPEntity getGp() {
        return gp;
    }

//    public void setGp(GPEntity gp) {
        this.gp = gp;
    }
*/
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

	public Date getRegistrationStartDateTime() {
		return registrationStartDateTime;
	}

	public Date getRegistrationEndDateTime() {
		return registrationEndDateTime;
	}

	public String getRegistrationStatus() {
		return registrationStatus;
	}

	public String getRegistrationType() {
		return registrationType;
	}

	public void setRegistrationStartDateTime(Date registrationStartDateTime) {
		this.registrationStartDateTime = registrationStartDateTime;
	}

	public void setRegistrationEndDateTime(Date registrationEndDateTime) {
		this.registrationEndDateTime = registrationEndDateTime;
	}

	public void setRegistrationStatus(String registrationStatus) {
		this.registrationStatus = registrationStatus;
	}

	public void setRegistrationType(String registrationType) {
		this.registrationType = registrationType;
	}

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    // Patient IDENTIFIERS
    @OneToMany(mappedBy="patientEntity", targetEntity=PatientIdentifier.class)
    private List<PatientIdentifier> identifiers;
    public void setIdentifiers(List<PatientIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
    public List<PatientIdentifier> getIdentifiers( ) {
        if (identifiers == null) {
            identifiers = new ArrayList<PatientIdentifier>();
        }
        return this.identifiers;
    }
    public List<PatientIdentifier> addIdentifier(PatientIdentifier pi) {
        identifiers.add(pi);
        return identifiers; }

    public List<PatientIdentifier> removeIdentifier(PatientIdentifier identifier){
        identifiers.remove(identifiers); return identifiers; }


}
