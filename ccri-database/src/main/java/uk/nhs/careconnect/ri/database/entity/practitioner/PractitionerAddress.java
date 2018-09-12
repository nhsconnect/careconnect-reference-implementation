package uk.nhs.careconnect.ri.database.entity.practitioner;


import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerAddress",indexes =
        {
                @Index(name = "IDX_PractitionerAddress_ADDRESS_ID", columnList="ADDRESS_ID"),
                @Index(name = "IDX_PractitionerAddress_PRACTITIONER_ID", columnList="PRACTITIONER_ID")


        })
public class PractitionerAddress extends BaseAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_ADDRESS_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ADDRESS_ADDRESS_ID"))

    private AddressEntity address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ADDRESS_PRACTITIONER_ID"))
    private PractitionerEntity practitionerEntity;


    public Long getId()
    {
        return this.myId;
    }

    public PractitionerEntity getPractitioner() {
        return this.practitionerEntity;
    }
    public void setPractitioner(PractitionerEntity practitionerEntity) {
        this.practitionerEntity = practitionerEntity;
    }

    @Override
    public AddressEntity getAddress() {
        return this.address;
    }
    @Override
    public AddressEntity setAddress(AddressEntity addressEntity) {
        this.address = addressEntity;
        return this.address;
    }



}
