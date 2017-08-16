package uk.gov.hscic.location;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "locations")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
    @Column(name = "name")
	private String name;
	
    @Column(name = "org_ods_code")
    private String orgOdsCode;
	
    @Column(name = "org_ods_code_name")
    private String orgOdsCodeName;
    
    @Column(name = "site_ods_code")
    private String siteOdsCode;
	
    @Column(name = "site_ods_code_name")
    private String siteOdsCodeName;
    
    @Column(name = "lastUpdated")
    private Date lastUpdated;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getOrgOdsCode() {
        return orgOdsCode;
    }

    public void setOrgOdsCode(String orgOdsCode) {
        this.orgOdsCode = orgOdsCode;
    }

    public String getOrgOdsCodeName() {
        return orgOdsCodeName;
    }

    public void setOrgOdsCodeName(String orgOdsCodeName) {
        this.orgOdsCodeName = orgOdsCodeName;
    }

	public String getSiteOdsCode() {
		return siteOdsCode;
	}

	public void setSiteOdsCode(String siteOdsCode) {
		this.siteOdsCode = siteOdsCode;
	}

	public String getSiteOdsCodeName() {
		return siteOdsCodeName;
	}

	public void setSiteOdsCodeName(String siteOdsCodeName) {
		this.siteOdsCodeName = siteOdsCodeName;
	}

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
