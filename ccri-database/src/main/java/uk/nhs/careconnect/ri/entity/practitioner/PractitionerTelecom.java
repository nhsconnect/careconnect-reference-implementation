package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.BaseContactPoint;

import javax.persistence.*;


@Entity
@Table(name="PractitionerTelecom", uniqueConstraints= @UniqueConstraint(name="PK_PRACTITIONER_TELECOM", columnNames={"PRACTITIONER_TELECOM_ID"}))
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

	@ManyToOne
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_PRACTITIONER_TELECOM"))
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
