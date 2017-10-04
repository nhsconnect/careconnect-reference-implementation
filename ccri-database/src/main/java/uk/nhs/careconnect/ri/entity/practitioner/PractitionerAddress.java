package uk.nhs.careconnect.ri.entity.practitioner;


import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.BaseAddress;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerAddress")
public class PractitionerAddress extends BaseAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_ADDRESS_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

    @ManyToOne
    @JoinColumn(name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_PRACTITIONER_ADDRESS"))
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
    public AddressEntity setAddress(AddressEntity addressEntity) {
        this.address = addressEntity;
        return this.address;
    }



}
