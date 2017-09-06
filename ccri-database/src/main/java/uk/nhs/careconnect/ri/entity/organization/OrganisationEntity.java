package uk.nhs.careconnect.ri.entity.organization;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.patient.PatientIdentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "Organisation")
public class OrganisationEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ORGANISATION_ID")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifiedDate", nullable = true)
    private Date updated;
    public Date getUpdatedDate() { return updated; }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdDate", nullable = true)
    private Date createdDate;
    public Date getCreatedDate() { return createdDate; }

    @Column(name = "org_code")
    private String orgCode;
    
    @Column(name = "site_code")
    private String siteCode;
    
    @Column(name = "org_name")
    private String orgName;
    
    @Column(name = "lastUpdated")
    private Date lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Organisation IDENTIFIERS
    @OneToMany(mappedBy="organisationEntity", targetEntity=OrganisationIdentifier.class)
    private List<OrganisationIdentifier> identifiers;
    public void setIdentifiers(List<OrganisationIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
    public List<OrganisationIdentifier> getIdentifiers( ) {
        if (identifiers == null) {
            identifiers = new ArrayList<OrganisationIdentifier>();
        }
        return this.identifiers;
    }
    public List<OrganisationIdentifier> addIdentifier(OrganisationIdentifier pi) {
        identifiers.add(pi);
        return identifiers; }

    public List<OrganisationIdentifier> removeIdentifier(OrganisationIdentifier identifier){
        identifiers.remove(identifiers); return identifiers; }
}
