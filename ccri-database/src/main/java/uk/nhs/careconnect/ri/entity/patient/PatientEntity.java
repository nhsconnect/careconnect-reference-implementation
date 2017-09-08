
package uk.nhs.careconnect.ri.entity.patient;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "give_name")
    private String givenName;

    @Column(name = "famil_yname")
    private String familyName;


    @Column(name = "phone")
    private String phone;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastUpdated")
    private Date lastUpdated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registration_start")
    private Date registrationStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registration_end")
    private Date registrationEndDateTime;


    @Column(name = "sensitive_flag")
    private boolean sensitive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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



	public void setRegistrationStartDateTime(Date registrationStartDateTime) {
		this.registrationStartDateTime = registrationStartDateTime;
	}

	public void setRegistrationEndDateTime(Date registrationEndDateTime) {
		this.registrationEndDateTime = registrationEndDateTime;
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

    // Patient Address
    @OneToMany(mappedBy="patientEntity", targetEntity=PatientAddress.class)
    private List<PatientAddress> addresses;
    public void setAddresseses(List<PatientAddress> addresses) {
        this.addresses = addresses;
    }
    public List<PatientAddress> getAddresses( ) {
        if (addresses == null) {
            addresses = new ArrayList<PatientAddress>();
        }
        return this.addresses;
    }
    public List<PatientAddress> addAddress(PatientAddress pi) {
        addresses.add(pi);
        return addresses; }

    public List<PatientAddress> removeAddress(PatientAddress address){
        addresses.remove(address); return addresses; }
}
