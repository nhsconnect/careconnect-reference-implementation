
package uk.nhs.careconnect.ri.entity.patient;

import org.hl7.fhir.instance.model.HumanName;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
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

    @Enumerated(EnumType.ORDINAL)
    private HumanName.NameUse nameUse;

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


    public HumanName.NameUse getNameUse() {
        return this.nameUse;
    }

    public void setNameUse(HumanName.NameUse nameUse) {
        this.nameUse = nameUse;
    }

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

    // Patient IDENTIFIERS
    @OneToMany(mappedBy="patientEntity", targetEntity=PatientIdentifier.class)
    private List<PatientIdentifier> identifiers = new ArrayList<>();
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


    // Patient Telecom
    @OneToMany(mappedBy="patientEntity", targetEntity=PatientTelecom.class)
    private List<PatientTelecom> telecoms;
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
