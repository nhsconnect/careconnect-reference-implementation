
package uk.nhs.careconnect.ri.entity.patient;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

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

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "family_name")
    private String familyName;



    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registration_start")
    private Date registrationStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registration_end")
    private Date registrationEndDateTime;

    @ManyToOne
    @JoinColumn (name = "GP_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PRACTITIONER"))
    private PractitionerEntity gp;

    @ManyToOne
    @JoinColumn(name="PRACTICE_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ORGANISATION"))
    private OrganisationEntity practice;

    /*
    @Column(name = "sensitive_flag")
    private boolean sensitive;
*/
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

/*
    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }
*/
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

    public OrganisationEntity getPractice() {
        return practice;
    }
    public void setPractice(OrganisationEntity org) {
        this.practice = org;
    }

    public PractitionerEntity getGP() {
        return gp;
    }
    public void setGp(PractitionerEntity gp) { this.gp = gp; }
}
