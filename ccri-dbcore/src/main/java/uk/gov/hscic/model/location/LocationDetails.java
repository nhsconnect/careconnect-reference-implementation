package uk.gov.hscic.model.location;

import java.io.Serializable;
import java.util.Date;

public class LocationDetails implements Serializable {
    private Long id;
    private String name;
    private String orgOdsCode;
    private String orgOdsCodeName;
    private String siteOdsCode;
    private String siteOdsCodeName;
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
