package uk.nhs.careconnect.ri.database.entity.relatedPerson;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;


import javax.persistence.*;


@Entity
@Table(name="RelatedPersonTelecom",
		uniqueConstraints= @UniqueConstraint(name="PK_PERSON_TELECOM", columnNames={"PERSON_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_PERSON_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID")
		})
public class RelatedPersonTelecom extends BaseContactPoint {

	public RelatedPersonTelecom() {

	}

	public RelatedPersonTelecom(RelatedPersonEntity personEntity) {
		this.personEntity = personEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PERSON_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_PERSON_TELECOM"))
	private RelatedPersonEntity personEntity;


    public Long getTelecomId() { return telecomId; }
	public void setTelecomId(Long telecomId) { this.telecomId = telecomId; }

	public RelatedPersonEntity getRelatedPerson() {
	        return this.personEntity;
	}
	public void setRelatedPersonEntity(RelatedPersonEntity organisationEntity) {
	        this.personEntity = personEntity;
	}

}
