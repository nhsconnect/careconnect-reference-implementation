package uk.nhs.careconnect.ri.database.entity.practitioner;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;

import javax.persistence.*;


@Entity
@Table(name="PractitionerTelecom", uniqueConstraints= @UniqueConstraint(name="PK_PRACTITIONER_TELECOM", columnNames={"PRACTITIONER_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_PRACTITIONER_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_PRACTITIONER_TELECOM_PRACTITIONER_ID", columnList="PRACTITIONER_ID")
		})
public class PractitionerTelecom extends BaseContactPoint {

	public PractitionerTelecom() {

	}

	public PractitionerTelecom(PractitionerEntity practitionerEntity) {
		this.practitionerEntity = practitionerEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PRACTITIONER_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_TELECOM_PRACTITIONER_ID"))
	private PractitionerEntity practitionerEntity;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public PractitionerEntity getPractitioner() {
	        return this.practitionerEntity;
	}
	public void setPractitionerEntity(PractitionerEntity organisationEntity) {
	        this.practitionerEntity = practitionerEntity;
	}

}
