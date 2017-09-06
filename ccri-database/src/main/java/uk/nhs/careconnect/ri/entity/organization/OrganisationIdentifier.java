package uk.nhs.careconnect.ri.entity.organization;

import uk.nhs.careconnect.ri.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@Entity
@Table(name="OrganisationIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_ORGANISATION_IDENTIFIER", columnNames={"ORGANISATION_IDENTIFIER_ID"}))
public class OrganisationIdentifier extends BaseIdentifier {

	public OrganisationIdentifier() {

	}

	public OrganisationIdentifier(OrganisationEntity organisationEntity) {
		this.organisationEntity = organisationEntity;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ORGANISATION_IDENTIFIER_ID")
	public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }
	private Integer identifierId;
	
	private OrganisationEntity organisationEntity;

	@ManyToOne
	@JoinColumn (name = "ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_ORGANISATION_ORGANISATION_IDENTIFIER"))
	public OrganisationEntity getOrganisationEntity() {
	        return this.organisationEntity;
	}
	public void setOrganisationEntity(OrganisationEntity organisationEntity) {
	        this.organisationEntity = organisationEntity;
	}

    private SystemEntity systemEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SYSTEM_ID",foreignKey= @ForeignKey(name="FK_SYSTEM_ORGANISATION_IDENTIFIER"))
    public SystemEntity getSystem() {
        return this.systemEntity;
    }
    public void setSystem(SystemEntity systemEntity) {
        this.systemEntity = systemEntity;
    }
}
