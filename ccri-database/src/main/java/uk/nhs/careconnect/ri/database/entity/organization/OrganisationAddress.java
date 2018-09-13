package uk.nhs.careconnect.ri.database.entity.organization;


import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;

import javax.persistence.*;

@Entity
@Table(name = "OrganisationAddress",indexes =
        {
                @Index(name = "IDX_ORGANISATION_ADDRESS_ADDRESS_ID", columnList="ADDRESS_ID"),
                @Index(name = "IDX_ORGANISATION_ADDRESS_ORGANISATION_ID", columnList="ORGANISATION_ID")


        })
public class OrganisationAddress extends BaseAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ORGANISATION_ADDRESS_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID",foreignKey= @ForeignKey(name="FK_ORGANISATION_ADDRESS_ADDRESS_ID"))
    private AddressEntity address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_ORGANISATION_ADDRESS_ORGANISATION_ID"))

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

    @Override
    public AddressEntity getAddress() {
        return this.address;
    }
    @Override
    public AddressEntity setAddress(AddressEntity addressEntity) {
        this.address = addressEntity;
        return addressEntity;
    }


    @Override
    public Long getId() {
        return this.myId;
    }
}
