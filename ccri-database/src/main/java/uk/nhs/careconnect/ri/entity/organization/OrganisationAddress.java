package uk.nhs.careconnect.ri.entity.organization;


import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.BaseAddress;

import javax.persistence.*;

@Entity
@Table(name = "OrganisationAddress")
public class OrganisationAddress extends BaseAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ORGANISATION_ADDRESS_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_ORGANISATION_ORGANISATION_ADDRESS"))
    private OrganisationEntity organisationEntity;



    public Long getPID()
    {
        return this.myId;
    }

    public OrganisationEntity getOrganisation() {
        return this.organisationEntity;
    }
    public void setOrganisation(OrganisationEntity organisationEntity) {
        this.organisationEntity = organisationEntity;
    }


    public AddressEntity getAddress() {
        return this.address;
    }
    public AddressEntity setAddress(AddressEntity addressEntity) {
        this.address = addressEntity;
        return addressEntity;
    }



}
