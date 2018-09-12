package uk.nhs.careconnect.ri.database.entity.organization;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;

import javax.persistence.*;


@Entity
@Table(name="OrganisationTelecom",
		uniqueConstraints= @UniqueConstraint(name="PK_ORGANISATION_TELECOM", columnNames={"ORGANISATION_TELECOM_ID"}),indexes = {
				@Index(name = "IDX_ORGANISATION_TELECOM_ORGANISATION_ID", columnList = "ORGANISATION_ID")
		}
)
public class OrganisationTelecom extends BaseContactPoint {

	public OrganisationTelecom() {

	}

	public OrganisationTelecom(OrganisationEntity organisationEntity) {
		this.organisationEntity = organisationEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ORGANISATION_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_ORGANISATION_ORGANISATION_TELECOM"))
	private OrganisationEntity organisationEntity;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public OrganisationEntity getOrganization() {
	        return this.organisationEntity;
	}
	public void setOrganizationEntity(OrganisationEntity organisationEntity) {
	        this.organisationEntity = organisationEntity;
	}

}
