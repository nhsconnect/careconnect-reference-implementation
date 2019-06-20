package uk.nhs.careconnect.ri.database.entity.claim;

import uk.nhs.careconnect.ri.database.entity.BaseCodeableConcept;
import javax.persistence.*;

@Entity
@Table(name="ClaimType")
public class ClaimType extends BaseCodeableConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CLAIM_TYPE_ID")
    private Long Id;

    @OneToOne(mappedBy = "type")
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
