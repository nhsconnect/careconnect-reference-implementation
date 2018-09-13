
package uk.nhs.careconnect.ri.database.entity.relatedPerson;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "RelatedPerson",
indexes = {
        @Index(name = "IDX_PERSON_DOB", columnList="date_of_birth"),
})
public class RelatedPersonEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PERSON_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PERSON_PATIENT_ID"))
    private PatientEntity patient;

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
    @JoinColumn (name = "RELATIONSHIP_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_RELATIONSHIP_CONCEPT_ID"))
    private ConceptEntity relationship;

    @Column(name="active")
    private Boolean active;

    // RelatedPerson IDENTIFIERS
    @OneToMany(mappedBy="personEntity", targetEntity=RelatedPersonIdentifier.class)
    private List<RelatedPersonIdentifier> identifiers = new ArrayList<>();

    @OneToMany(mappedBy="personEntity", targetEntity=RelatedPersonAddress.class)
    private List<RelatedPersonAddress> addresses = new ArrayList<>();

  
    @OneToMany(mappedBy="personEntity", targetEntity=RelatedPersonName.class)
    private List<RelatedPersonName> names = new ArrayList<>();

    @OneToMany(mappedBy="personEntity", targetEntity=RelatedPersonTelecom.class)
    private List<RelatedPersonTelecom> telecoms = new ArrayList<>();

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


    public void setIdentifiers(List<RelatedPersonIdentifier> identifiers) {
        if (identifiers == null) {
            throw new NullPointerException("Identifiers cannot be null");
        }

        this.identifiers = identifiers;
    }
    public List<RelatedPersonIdentifier> getIdentifiers( ) {
        return this.identifiers;
    }
    public List<RelatedPersonIdentifier> addIdentifier(RelatedPersonIdentifier pi) {
        identifiers.add(pi);
        return identifiers; }

    public List<RelatedPersonIdentifier> removeIdentifier(RelatedPersonIdentifier identifier){
        identifiers.remove(identifiers); return identifiers; }

    // Patint Name

    public List<RelatedPersonName> getNames() {
        if (names == null) {
            names = new ArrayList<RelatedPersonName>();
        }
        return names;
    }

    public List<RelatedPersonName> setNames(List<RelatedPersonName> names) {
        this.names = names;
        return names;
    }

    public RelatedPersonName addName() {
        RelatedPersonName name = new RelatedPersonName();
        this.getNames().add(name);
        return name;
    }


    // RelatedPerson Address

    public void setAddresseses(List<RelatedPersonAddress> addresses) {
        this.addresses = addresses;
    }
    public List<RelatedPersonAddress> getAddresses( ) {
        if (addresses == null) {
            addresses = new ArrayList<RelatedPersonAddress>();
        }
        return this.addresses;
    }
    public List<RelatedPersonAddress> addAddress(RelatedPersonAddress pi) {
        addresses.add(pi);
        return addresses; }

    public List<RelatedPersonAddress> removeAddress(RelatedPersonAddress address){
        addresses.remove(address); return addresses; }



    public Boolean getActive() {
        return active;
    }

    public void setAddresses(List<RelatedPersonAddress> addresses) {
        this.addresses = addresses;
    }

    public ConceptEntity getRelationship() {
        return relationship;
    }

    public void setRelationship(ConceptEntity relationship) {
        this.relationship = relationship;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public List<RelatedPersonTelecom> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(List<RelatedPersonTelecom> telecoms) {
        this.telecoms = telecoms;
    }

    public List<RelatedPersonTelecom> addTelecom(RelatedPersonTelecom pi) {
        telecoms.add(pi);
        return telecoms;
    }
}
