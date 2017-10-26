
package uk.nhs.careconnect.ri.entity.patient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "Patient",
indexes = {
        @Index(name = "IDX_PATIENT_DOB", columnList="date_of_birth"),
})
public class PatientEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PATIENT_ID")
    private Long id;



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
    @LazyCollection(LazyCollectionOption.TRUE)
    private PractitionerEntity gp;

    @ManyToOne
    @JoinColumn(name="PRACTICE_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ORGANISATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private OrganisationEntity practice;

    @ManyToOne
    @JoinColumn(name="ethnic")

    private ConceptEntity ethnicCode;

    @ManyToOne
    @JoinColumn(name="marital")
    private ConceptEntity maritalCode;

    @ManyToOne
    @JoinColumn(name="NHSverification")
    private ConceptEntity NHSVerificationCode;

    @Column(name="active")
    private Boolean active;

    // Patient IDENTIFIERS
    @OneToMany(mappedBy="patientEntity", targetEntity=PatientIdentifier.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private List<PatientIdentifier> identifiers = new ArrayList<>();

    @OneToMany(mappedBy="patientEntity", targetEntity=PatientAddress.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private List<PatientAddress> addresses;

    @OneToMany(mappedBy="patientEntity", targetEntity=PatientTelecom.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private List<PatientTelecom> telecoms;

    @OneToMany(mappedBy="patientEntity", targetEntity=PatientName.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private List<PatientName> names;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public void setActive(Boolean active) {
        this.active = active;
    }
    public Boolean getActiveRecord() {
        return this.active;
    }

    public ConceptEntity getEthnicCode() {
        return this.ethnicCode;
    }
    public ConceptEntity getMaritalCode() {
        return this.maritalCode;
    }
    public ConceptEntity getNHSVerificationCode() {
        return this.NHSVerificationCode;
    }
    public void setEthnicCode (ConceptEntity code) {
        this.ethnicCode = code;
    }
    public void setMaritalCode(ConceptEntity code) {
        this.maritalCode = code;
    }
    public void setNHSVerificationCode(ConceptEntity code) {
        this.NHSVerificationCode = code;
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


    public void setIdentifiers(List<PatientIdentifier> identifiers) {
        if (identifiers == null) {
            throw new NullPointerException("Identifiers cannot be null");
        }

        this.identifiers = identifiers;
    }
    public List<PatientIdentifier> getIdentifiers( ) {
        return this.identifiers;
    }
    public List<PatientIdentifier> addIdentifier(PatientIdentifier pi) {
        identifiers.add(pi);
        return identifiers; }

    public List<PatientIdentifier> removeIdentifier(PatientIdentifier identifier){
        identifiers.remove(identifiers); return identifiers; }

    // Patint Name

    public List<PatientName> getNames() {
        if (names == null) {
            names = new ArrayList<PatientName>();
        }
        return names;
    }

    public List<PatientName> setNames(List<PatientName> names) {
        this.names = names;
        return names;
    }

    public PatientName addName() {
        PatientName name = new PatientName();
        return name;
    }


    // Patient Address

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


    // Patient Telecom

    public void setTelecoms(List<PatientTelecom> telecoms) {
        this.telecoms = telecoms;
    }
    public List<PatientTelecom> getTelecoms( ) {
        if (telecoms == null) {
            telecoms = new ArrayList<PatientTelecom>();
        }
        return this.telecoms;
    }
    public List<PatientTelecom> addTelecom(PatientTelecom pi) {
        telecoms.add(pi);
        return telecoms; }

    public List<PatientTelecom> removeTelecom(PatientTelecom telecom){
        addresses.remove(telecom); return telecoms; }


}
