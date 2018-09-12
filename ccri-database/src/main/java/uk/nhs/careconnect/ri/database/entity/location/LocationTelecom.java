package uk.nhs.careconnect.ri.database.entity.location;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;

import javax.persistence.*;


@Entity
@Table(name="LocationTelecom",
		uniqueConstraints= @UniqueConstraint(name="PK_LOCATION_TELECOM", columnNames={"LOCATION_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_LOCATION_TELECOM_LOCATION_ID", columnList="LOCATION_ID")
		})
public class LocationTelecom extends BaseContactPoint {

	public LocationTelecom() {

	}

	public LocationTelecom(LocationEntity locationEntity) {
		this.locationEntity = locationEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "LOCATION_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "LOCATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_LOCATION_TELECOM"))
	private LocationEntity locationEntity;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public LocationEntity getLocation() {
	        return this.locationEntity;
	}
	public void setLocation(LocationEntity organisationEntity) {
	        this.locationEntity = locationEntity;
	}

}
