package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;

@Entity
@Table(name = "Practitioner")
public class PractitionerEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_ID")
    private Long id;

    @Column(name = "userid")
    private String userId;

    @Column(name = "role_ids")
    private String roleIds;

    @Column(name = "name_family")
    private String nameFamily;

    @Column(name = "name_given")
    private String nameGiven;

    @Column(name = "name_prefix")
    private String namePrefix;

    @Column(name = "gender")
    private String gender;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "role_code")
    private String roleCode;

    @Column(name = "role_display")
    private String roleDisplay;

    @Column(name = "com_code")
    private String comCode;

    @Column(name = "com_display")
    private String comDisplay;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(String roleIds) {
        this.roleIds = roleIds;
    }

    public String getNameFamily() {
        return nameFamily;
    }

    public void setNameFamily(String nameFamily) {
        this.nameFamily = nameFamily;
    }

    public String getNameGiven() {
        return nameGiven;
    }

    public void setNameGiven(String nameGiven) {
        this.nameGiven = nameGiven;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleDisplay() {
        return roleDisplay;
    }

    public void setRoleDisplay(String roleDisplay) {
        this.roleDisplay = roleDisplay;
    }

    public String getComCode() {
        return comCode;
    }

    public void setComCode(String comCode) {
        this.comCode = comCode;
    }

    public String getComDisplay() {
        return comDisplay;
    }

    public void setComDisplay(String comDisplay) {
        this.comDisplay = comDisplay;
    }

}
