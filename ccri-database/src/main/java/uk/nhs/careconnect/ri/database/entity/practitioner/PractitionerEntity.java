package uk.nhs.careconnect.ri.database.entity.practitioner;

import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Practitioner")
public class PractitionerEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_ID")
    private Long id;

    @Column(name = "userid")
    private String userId;

    @Column(name = "gender")
    private String gender;

    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerIdentifier.class)

    private List<PractitionerIdentifier> identifiers;

    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerAddress.class)

    private List<PractitionerAddress> addresses;

    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerName.class)

    private List<PractitionerName> names;

    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerRole.class)

    private List<PractitionerRole> roles;

    @Column(name="active")
    private Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

 // Practitioner Name

    public List<PractitionerName> setNames(List<PractitionerName> names) {
        this.names = names;
        return names;
    }

    public List<PractitionerName> getNames() {
        if (names == null) {
            names = new ArrayList<PractitionerName>();
        }
        return names;
    }
    // Practitioner IDENTIFIERS

    public void setIdentifiers(List<PractitionerIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
    public List<PractitionerIdentifier> getIdentifiers( ) {
        if (identifiers == null) {
            identifiers = new ArrayList<PractitionerIdentifier>();
        }
        return this.identifiers;
    }
    public List<PractitionerIdentifier> addIdentifier(PractitionerIdentifier pi) {
        identifiers.add(pi);
        return identifiers; }

    public List<PractitionerIdentifier> removeIdentifier(PractitionerIdentifier identifier){
        identifiers.remove(identifiers); return identifiers; }

    // Practitioner Address

    public void setAddresseses(List<PractitionerAddress> addresses) {
        this.addresses = addresses;
    }
    public List<PractitionerAddress> getAddresses( ) {
        if (addresses == null) {
            addresses = new ArrayList<PractitionerAddress>();
        }
        return this.addresses;
    }
    public List<PractitionerAddress> addAddress(PractitionerAddress pi) {
        addresses.add(pi);
        return addresses; }

    public List<PractitionerAddress> removeAddress(PractitionerAddress address){
        addresses.remove(address); return addresses; }

    // Practitioner Telecom
    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerTelecom.class)
    private List<PractitionerTelecom> telecoms;
    public void setTelecoms(List<PractitionerTelecom> telecoms) {
        this.telecoms = telecoms;
    }
    public List<PractitionerTelecom> getTelecoms( ) {
        if (telecoms == null) {
            telecoms = new ArrayList<PractitionerTelecom>();
        }
        return this.telecoms;
    }
    public List<PractitionerTelecom> addTelecom(PractitionerTelecom pi) {
        telecoms.add(pi);
        return telecoms; }

    public List<PractitionerTelecom> removeTelecom(PractitionerTelecom telecom){
        addresses.remove(telecom); return telecoms; }



    public List<PractitionerRole> getRoles() {
        if (roles == null) {
            roles = new ArrayList<PractitionerRole>();
        }
        return roles;
    }

    public void setRoles(List<PractitionerRole> roles) {
        this.roles = roles;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }
}
