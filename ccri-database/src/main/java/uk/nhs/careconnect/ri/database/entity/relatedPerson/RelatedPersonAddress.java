package uk.nhs.careconnect.ri.database.entity.relatedPerson;


import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;

import javax.persistence.*;

@Entity
@Table(name = "RelatedPersonAddress")
public class RelatedPersonAddress extends BaseAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PERSON_ADDRESS_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID",foreignKey= @ForeignKey(name="FK_PERSON_ADDRESS_ADDRESS"))
    private AddressEntity address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_ADDRESS_PERSON"))
    private RelatedPersonEntity personEntity;

    public Long getId()
    {
        return this.myId;
    }

    public RelatedPersonEntity getRelatedPersonEntity() {
        return this.personEntity;
    }
    public void setRelatedPersonEntity(RelatedPersonEntity personEntity) {
        this.personEntity = personEntity;
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
