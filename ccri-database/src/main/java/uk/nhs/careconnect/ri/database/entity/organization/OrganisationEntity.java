package uk.nhs.careconnect.ri.database.entity.organization;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Organisation",indexes =
        {
                @Index(name = "IDX_ORGANISATION_NAME", columnList="ENT_NAME")

        })
public class OrganisationEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ORGANISATION_ID")
    private Long id;


    @Column(name = "ENT_NAME")
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="partOf",foreignKey= @ForeignKey(name="FK_ORGANISATION_PART_OF_ORGANISATION"))
    private OrganisationEntity partOf;

    @Column(name="active")
    private Boolean active;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ORGANISATION_TYPE_CONCEPT_ID"))
    private ConceptEntity type;

    @OneToMany(mappedBy="organisationEntity", targetEntity=OrganisationIdentifier.class)

    private List<OrganisationIdentifier> identifiers;

    @OneToMany(mappedBy="organisationEntity", targetEntity=OrganisationAddress.class)

    private List<OrganisationAddress> addresses;


    @OneToMany(mappedBy="organisationEntity", targetEntity=OrganisationTelecom.class)
    private List<OrganisationTelecom> telecoms;




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

    public void setAddresseses(List<OrganisationAddress> addresses) {
        this.addresses = addresses;
    }
    public List<OrganisationAddress> getAddresses( ) {
        if (addresses == null) {
            addresses = new ArrayList<OrganisationAddress>();
        }
        return this.addresses;
    }
    public OrganisationAddress addAddress(OrganisationAddress pi) {
        addresses.add(pi);
        return pi; }

    public List<OrganisationAddress> removeAddress(OrganisationAddress address){
        addresses.remove(address); return addresses; }

    // Organisation Telecom

    public void setTelecoms(List<OrganisationTelecom> telecoms) {
        this.telecoms = telecoms;
    }
    public List<OrganisationTelecom> getTelecoms( ) {
        if (telecoms == null) {
            telecoms = new ArrayList<OrganisationTelecom>();
        }
        return this.telecoms;
    }
    public OrganisationTelecom addTelecom(OrganisationTelecom pi) {
        telecoms.add(pi);
        return pi; }

    public List<OrganisationTelecom> removeTelecom(OrganisationTelecom telecom){
        addresses.remove(telecom); return telecoms; }

    public void setPartOf(OrganisationEntity partOf) {
        this.partOf = partOf;
    }

    public OrganisationEntity getPartOf() {
        return partOf;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public ConceptEntity getType() {
        return type;
    }

    public void setType(ConceptEntity type) {
        this.type = type;
    }
}
