
package uk.nhs.careconnect.ri.database.entity.patient;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "GP_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PRACTITIONER"))

    private PractitionerEntity gp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PRACTICE_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ORGANISATION"))

    private OrganisationEntity practice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ethnic",foreignKey= @ForeignKey(name="FK_PATIENT_ETHNIC_CONCEPT_ID"))

    private ConceptEntity ethnicCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="marital",foreignKey= @ForeignKey(name="FK_PATIENT_MARITAL_CONCEPT_ID"))

    private ConceptEntity maritalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="NHSverification",foreignKey= @ForeignKey(name="FK_PATIENT_NHSVERIFICATION_CONCEPT_ID"))

    private ConceptEntity NHSVerificationCode;

    @Column(name="active")
    private Boolean active;

    // Patient IDENTIFIERS
    @OneToMany(mappedBy="patientEntity", targetEntity=PatientIdentifier.class)

    private List<PatientIdentifier> identifiers = new ArrayList<>();

    @OneToMany(mappedBy="patientEntity", targetEntity=PatientAddress.class)

    private List<PatientAddress> addresses;

    @OneToMany(mappedBy="patientEntity", targetEntity=PatientTelecom.class)

    private List<PatientTelecom> telecoms;

    @OneToMany(mappedBy="patientEntity", targetEntity=PatientName.class)

    private List<PatientName> names;


    // For Reverse Includes

    @OneToMany(mappedBy="patient", targetEntity = ProcedureEntity.class)

    Set<ProcedureEntity> patientProcedures = new HashSet<>();

    @OneToMany(mappedBy="patient", targetEntity = ObservationEntity.class)

    Set<ObservationEntity> patientObservations = new HashSet<>();

    @OneToMany(mappedBy="patient", targetEntity = ConditionEntity.class)

    Set<ConditionEntity> patientConditions = new HashSet<>();

    @OneToMany(mappedBy="patient", targetEntity = MedicationRequestEntity.class)

    Set<MedicationRequestEntity> patientMedicationRequests = new HashSet<>();

    @OneToMany(mappedBy="patient", targetEntity = EncounterEntity.class)

    Set<EncounterEntity> patientEncounters = new HashSet<>();

    @OneToMany(mappedBy="patient", targetEntity =AllergyIntoleranceEntity.class)

    Set<AllergyIntoleranceEntity> patientAlelrgies = new HashSet<>();

    @OneToMany(mappedBy="patient", targetEntity = ImmunisationEntity.class)

    Set<ImmunisationEntity> patientImmunisations = new HashSet<>();

    // Support for reverse includes

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
        this.getNames().add(name);
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
    public List<PatientTelecom> getTelecoms() {
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

    public PractitionerEntity getGp() {
        return gp;
    }

    public Boolean getActive() {
        return active;
    }

    public void setAddresses(List<PatientAddress> addresses) {
        this.addresses = addresses;
    }

    public Set<ProcedureEntity> getPatientProcedures() {
        return patientProcedures;
    }

    public void setPatientProcedures(Set<ProcedureEntity> patientProcedures) {
        this.patientProcedures = patientProcedures;
    }

    public Set<ObservationEntity> getPatientObservations() {
        return patientObservations;
    }

    public void setPatientObservations(Set<ObservationEntity> patientObservations) {
        this.patientObservations = patientObservations;
    }

    public Set<ConditionEntity> getPatientConditions() {
        return patientConditions;
    }

    public void setPatientConditions(Set<ConditionEntity> patientConditions) {
        this.patientConditions = patientConditions;
    }

    public Set<MedicationRequestEntity> getPatientMedicationRequests() {
        return patientMedicationRequests;
    }

    public void setPatientMedicationRequests(Set<MedicationRequestEntity> patientMedicationRequests) {
        this.patientMedicationRequests = patientMedicationRequests;
    }

    public Set<EncounterEntity> getPatientEncounters() {
        return patientEncounters;
    }

    public void setPatientEncounters(Set<EncounterEntity> patientEncounters) {
        this.patientEncounters = patientEncounters;
    }

    public Set<AllergyIntoleranceEntity> getPatientAlelrgies() {
        return patientAlelrgies;
    }

    public void setPatientAlelrgies(Set<AllergyIntoleranceEntity> patientAlelrgies) {
        this.patientAlelrgies = patientAlelrgies;
    }

    public Set<ImmunisationEntity> getPatientImmunisations() {
        return patientImmunisations;
    }

    public void setPatientImmunisations(Set<ImmunisationEntity> patientImmunisations) {
        this.patientImmunisations = patientImmunisations;
    }
}
