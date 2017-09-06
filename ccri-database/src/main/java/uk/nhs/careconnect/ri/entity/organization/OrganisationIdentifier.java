package uk.nhs.careconnect.ri.entity.organization;

import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@Entity
@Table(name="OrganisationIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_ORGANISATION_IDENTIFIER", columnNames={"ORGANISATION_IDENTIFIER_ID"}))
public class OrganisationIdentifier {

	public OrganisationIdentifier() {

	}

	public OrganisationIdentifier(OrganisationEntity organisationEntity) {
		this.organisationEntity = organisationEntity;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ORGANISATION_IDENTIFIER_ID")
	private Integer identifierId;


	@ManyToOne
	@JoinColumn (name = "ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_ORGANISATION_ORGANISATION_IDENTIFIER"))
	private OrganisationEntity organisationEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SYSTEM_ID",foreignKey= @ForeignKey(name="FK_SYSTEM_ORGANISATION_IDENTIFIER"))
	private SystemEntity systemEntity;

    @Column(name = "value")
    private String value;

    @Column(name = "ORDER")
    private Integer order;


    public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }

	public OrganisationEntity getOrganisation() {
	        return this.organisationEntity;
	}
	public void setOrganisation(OrganisationEntity organisationEntity) {
	        this.organisationEntity = organisationEntity;
	}

    public SystemEntity getSystem() {
        return this.systemEntity;
    }
    public void setSystem(SystemEntity systemEntity) {
        this.systemEntity = systemEntity;
    }

    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }
}
