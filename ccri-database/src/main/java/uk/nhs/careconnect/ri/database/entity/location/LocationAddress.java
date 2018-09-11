package uk.nhs.careconnect.ri.database.entity.location;


import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;

import javax.persistence.*;

@Entity
@Table(name = "LocationAddress",indexes =
        {
                @Index(name = "IDX_LOCATION_ADDRESS_LOCATION_ID", columnList="LOCATION_ID"),
                @Index(name = "IDX_LOCATION_ADDRESS_ADDRESS_ID", columnList="ADDRESS_ID")
        })
public class LocationAddress extends BaseAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="LOCATION_ADDRESS_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID",foreignKey= @ForeignKey(name="FK_LOCATION_ADDRESS_ADDRESS_ID"))

    private AddressEntity address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_ADDRESS_LOCATION_ID"))
    private LocationEntity locationEntity;
    public Long getId()
    {
        return this.myId;
    }

    public LocationEntity getPractitioner() {
        return this.locationEntity;
    }
    public void setLocation(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
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
