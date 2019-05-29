package uk.nhs.careconnect.ri.database.entity.claim;

import uk.nhs.careconnect.ri.database.entity.BaseCodeableConcept;

import javax.persistence.*;

@Entity
@Table(name="ClaimSubType")
public class ClaimSubType extends BaseCodeableConcept {

    @Id
    @Column(name= "CLAIM_SUB_TYPE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @OneToOne
    @MapsId("_ID")
    private ClaimEntity claim;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ClaimEntity getClaim() {
        return claim;
    }

    public void setClaim(ClaimEntity claim) {
        this.claim = claim;
    }
}
