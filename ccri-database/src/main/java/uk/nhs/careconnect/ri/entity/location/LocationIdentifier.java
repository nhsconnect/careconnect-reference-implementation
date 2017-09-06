package uk.nhs.careconnect.ri.entity.location;

import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@Entity
@Table(name="LocationIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_LOCATION_IDENTIFIER", columnNames={"LOCATION_IDENTIFIER_ID"}))
public class LocationIdentifier  {

	public LocationIdentifier() {

	}

	public LocationIdentifier(LocationEntity locationEntity) {
		this.locationEntity = locationEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "LOCATION_IDENTIFIER_ID")
	private Integer identifierId;

	@ManyToOne
	@JoinColumn (name = "LOCATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_LOCATION_IDENTIFIER"))
	private LocationEntity locationEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SYSTEM_ID",foreignKey= @ForeignKey(name="FK_SYSTEM_LOCATION_IDENTIFIER"))
	private SystemEntity systemEntity;

    @Column(name = "value")
    private String value;


    @Column(name = "ORDER")
    private Integer order;


    public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }

	public LocationEntity getLocation() {
	        return this.locationEntity;
	}
	public void setLocationEntity(LocationEntity organisationEntity) {
	        this.locationEntity = locationEntity;
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
