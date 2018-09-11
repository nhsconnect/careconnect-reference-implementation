package uk.nhs.careconnect.ri.database.entity.location;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="LocationIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_LOCATION_IDENTIFIER", columnNames={"LOCATION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_LOCATION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_LOCATION_IDENTIFER_LOCATION_ID", columnList="LOCATION_ID")


		})
public class LocationIdentifier extends BaseIdentifier {

	public LocationIdentifier() {

	}

	public LocationIdentifier(LocationEntity locationEntity) {
		this.locationEntity = locationEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "LOCATION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "LOCATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_LOCATION_IDENTIFIER"))
	private LocationEntity locationEntity;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public LocationEntity getLocation() {
	        return this.locationEntity;
	}

	public void setLocation(LocationEntity locationEntity) {
	        this.locationEntity = locationEntity;
	}




}
