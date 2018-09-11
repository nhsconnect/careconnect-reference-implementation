package uk.nhs.careconnect.ri.database.entity.consent;

import javax.persistence.*;

@Entity
@Table(name="ConsentPolicy", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_POLICY", columnNames={"CONSENT_POLICY_ID"})

)
public class ConsentPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONSENT_POLICY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_POLICY_CONSENT_ID"))
    private ConsentEntity consent;

    @Column(name="AUTHORITY")
    private String authority;

    @Column(name="POLICY_URI")
    private String policyUri;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getPolicyUri() {
        return policyUri;
    }

    public void setPolicyUri(String policyUri) {
        this.policyUri = policyUri;
    }

    public ConsentEntity getConsent() {
        return consent;
    }


    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }
}
