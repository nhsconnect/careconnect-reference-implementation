package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "prefix")
    private String prefix;

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

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
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

    // Practitioner IDENTIFIERS
    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerIdentifier.class)
    private List<PractitionerIdentifier> identifiers;
    public void setIdentifiers(List<PractitionerIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
    public List<PractitionerIdentifier> getIdentifiers( ) {
        if (identifiers == null) {
            identifiers = new ArrayList<PractitionerIdentifier>();
        }
        return this.identifiers;
    }
    public List<PractitionerIdentifier> addIdentifier(PractitionerIdentifier pi) {
        identifiers.add(pi);
        return identifiers; }

    public List<PractitionerIdentifier> removeIdentifier(PractitionerIdentifier identifier){
        identifiers.remove(identifiers); return identifiers; }

    // Practitioner Address
    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerAddress.class)
    private List<PractitionerAddress> addresses;
    public void setAddresseses(List<PractitionerAddress> addresses) {
        this.addresses = addresses;
    }
    public List<PractitionerAddress> getAddresses( ) {
        if (addresses == null) {
            addresses = new ArrayList<PractitionerAddress>();
        }
        return this.addresses;
    }
    public List<PractitionerAddress> addAddress(PractitionerAddress pi) {
        addresses.add(pi);
        return addresses; }

    public List<PractitionerAddress> removeAddress(PractitionerAddress address){
        addresses.remove(address); return addresses; }

    // Practitioner Telecom
    @OneToMany(mappedBy="practitionerEntity", targetEntity=PractitionerTelecom.class)
    private List<PractitionerTelecom> telecoms;
    public void setTelecoms(List<PractitionerTelecom> telecoms) {
        this.telecoms = telecoms;
    }
    public List<PractitionerTelecom> getTelecoms( ) {
        if (telecoms == null) {
            telecoms = new ArrayList<PractitionerTelecom>();
        }
        return this.telecoms;
    }
    public List<PractitionerTelecom> addTelecom(PractitionerTelecom pi) {
        telecoms.add(pi);
        return telecoms; }

    public List<PractitionerTelecom> removeTelecom(PractitionerTelecom telecom){
        addresses.remove(telecom); return telecoms; }
}
