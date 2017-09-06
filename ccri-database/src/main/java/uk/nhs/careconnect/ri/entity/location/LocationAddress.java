package uk.nhs.careconnect.ri.entity.location;


import uk.nhs.careconnect.ri.entity.AddressEntity;

import javax.persistence.*;

@Entity
@Table(name = "LocationAddress")
public class LocationAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="LOCATION_ADDRESS_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

    @ManyToOne
    @JoinColumn(name = "LOCATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_LOCATION_ADDRESS"))
    private LocationEntity locationEntity;

    public Long getPID()
    {
        return this.myId;
    }

    public LocationEntity getPractitioner() {
        return this.locationEntity;
    }
    public void setPractitioner(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
    }


    public AddressEntity getAddress() {
        return this.address;
    }
    public void setAddress(AddressEntity addressEntity) { this.address = addressEntity; }



}
