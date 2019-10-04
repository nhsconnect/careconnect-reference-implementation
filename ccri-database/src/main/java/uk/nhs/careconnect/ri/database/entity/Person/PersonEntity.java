package uk.nhs.careconnect.ri.database.entity.Person;

import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table
        (name = "Person"
)
public class PersonEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PERSON_ID")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name="active")
    private Boolean active;

    // Person IDENTIFIERS
    @OneToMany(mappedBy="personEntity", targetEntity=PersonIdentifier.class)
    private List<PersonIdentifier> identifiers = new ArrayList<>();


    @OneToMany(mappedBy="personEntity", targetEntity=PersonAddress.class)
    private List<PersonAddress> addresses = new ArrayList<>();

       @OneToMany(mappedBy="personEntity", targetEntity=PersonTelecom.class)
       private List<PersonTelecom> telecoms = new ArrayList<>();

    @OneToMany(mappedBy="personEntity", targetEntity=PersonName.class)
    private List<PersonName> names = new ArrayList<>();

    @Override
    public Long getId() {
         return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<PersonIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<PersonIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<PersonAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<PersonAddress> addresses) {
        this.addresses = addresses;
    }

    public List<PersonName> getNames() {
        return names;
    }

    public void setNames(List<PersonName> names) {
        this.names = names;
    }

    public List<PersonTelecom> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(List<PersonTelecom> telecoms) {
        this.telecoms = telecoms;
    }
}
