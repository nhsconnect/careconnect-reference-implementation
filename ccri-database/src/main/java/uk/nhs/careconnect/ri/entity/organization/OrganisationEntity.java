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
}
