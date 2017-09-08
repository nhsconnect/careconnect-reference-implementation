package uk.nhs.careconnect.ri.entity.organization;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Organisation")
public class OrganisationEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ORGANISATION_ID")
    private Long id;


    @Column(name = "name")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // Organisation IDENTIFIERS
    @OneToMany(mappedBy="organisationEntity", targetEntity=OrganisationIdentifier.class)
    private List<OrganisationIdentifier> identifiers;
    public void setIdentifiers(List<OrganisationIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
    public List<OrganisationIdentifier> getIdentifiers( ) {
        if (identifiers == null) {
            identifiers = new ArrayList<OrganisationIdentifier>();
        }
        return this.identifiers;
    }
    public List<OrganisationIdentifier> addIdentifier(OrganisationIdentifier pi) {
        identifiers.add(pi);
        return identifiers; }

    public List<OrganisationIdentifier> removeIdentifier(OrganisationIdentifier identifier){
        identifiers.remove(identifiers); return identifiers; }

    // Organisation Address
    @OneToMany(mappedBy="organisationEntity", targetEntity=OrganisationAddress.class)
    private List<OrganisationAddress> addresses;
    public void setAddresseses(List<OrganisationAddress> addresses) {
        this.addresses = addresses;
    }
    public List<OrganisationAddress> getAddresses( ) {
        if (addresses == null) {
            addresses = new ArrayList<OrganisationAddress>();
        }
        return this.addresses;
    }
    public List<OrganisationAddress> addAddress(OrganisationAddress pi) {
        addresses.add(pi);
        return addresses; }

    public List<OrganisationAddress> removeAddress(OrganisationAddress address){
        addresses.remove(address); return addresses; }

    // Organisation Telecom
    @OneToMany(mappedBy="organisationEntity", targetEntity=OrganisationTelecom.class)
    private List<OrganisationTelecom> telecoms;
    public void setTelecoms(List<OrganisationTelecom> telecoms) {
        this.telecoms = telecoms;
    }
    public List<OrganisationTelecom> getTelecoms( ) {
        if (telecoms == null) {
            telecoms = new ArrayList<OrganisationTelecom>();
        }
        return this.telecoms;
    }
    public List<OrganisationTelecom> addTelecom(OrganisationTelecom pi) {
        telecoms.add(pi);
        return telecoms; }

    public List<OrganisationTelecom> removeTelecom(OrganisationTelecom telecom){
        addresses.remove(telecom); return telecoms; }
}
