package uk.nhs.careconnect.ri.database.entity.claim;

import uk.nhs.careconnect.ri.database.entity.BaseCodeableConcept;

import javax.persistence.*;

@Entity
@Table(name="ClaimPriority")
public class ClaimPriority extends BaseCodeableConcept {

    @Id
    @Column(name= "CLAIM_PRIORITY_ID")
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
