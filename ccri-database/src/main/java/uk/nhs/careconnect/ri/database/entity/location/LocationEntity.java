package uk.nhs.careconnect.ri.database.entity.location;

import org.hl7.fhir.dstu3.model.Location;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Location",indexes =
		{
				@Index(name = "IDX_LOCATION_NAME", columnList="ENT_NAME")

		})
public class LocationEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="LOCATION_ID")
	private Long id;

    
    @Column(name = "ENT_NAME")
	private String name;

	@OneToMany(mappedBy="locationEntity", targetEntity=LocationIdentifier.class)

	private List<LocationIdentifier> identifiers;

	@OneToMany(mappedBy="locationEntity", targetEntity=LocationAddress.class)

	private List<LocationAddress> addresses;

	@OneToMany(mappedBy="locationEntity", targetEntity=LocationTelecom.class)

	private List<LocationTelecom> telecoms;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="MANAGING_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_ORGANISATION"))

	private OrganisationEntity managingOrganisation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_LOCATION_TYPE_CONCEPT"))
	private ConceptEntity type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="physicalType",foreignKey= @ForeignKey(name="FK_LOCATION_PHYSCIAL_TYPE_CONCEPT"))
	private ConceptEntity physicalType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="PART_OF_LOCATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_PARTOF_LOCATION"))

	private LocationEntity partOf;

	@Column(name="posn_longitude", precision=20, scale=10)
	private BigDecimal longitude;

	@Column(name="posn_latitude", precision=20, scale=10)
	private BigDecimal latitude;


	@Column(name="posn_altitude", precision=20, scale=10)
	private BigDecimal altitude;


	@Enumerated(EnumType.ORDINAL)
	Location.LocationStatus status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name.trim();
	}

	public void setName(String name) {
		this.name = name;
	}

	// Location IDENTIFIERS

	public void setIdentifiers(List<LocationIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	public List<LocationIdentifier> getIdentifiers( ) {
		if (identifiers == null) {
			identifiers = new ArrayList<LocationIdentifier>();
		}
		return this.identifiers;
	}
	public List<LocationIdentifier> addIdentifier(LocationIdentifier pi) {
		identifiers.add(pi);
		return identifiers; }

	public List<LocationIdentifier> removeIdentifier(LocationIdentifier identifier){
		identifiers.remove(identifiers); return identifiers; }

	// Location Address

	public void setAddresseses(List<LocationAddress> addresses) {
		this.addresses = addresses;
	}
	public List<LocationAddress> getAddresses( ) {
		if (addresses == null) {
			addresses = new ArrayList<LocationAddress>();
		}
		return this.addresses;
	}
	public List<LocationAddress> addAddress(LocationAddress pi) {
		addresses.add(pi);
		return addresses; }

	public List<LocationAddress> removeAddress(LocationAddress address){
		addresses.remove(address); return addresses; }

	// Location Telecom

	public void setTelecoms(List<LocationTelecom> telecoms) {
		this.telecoms = telecoms;
	}
	public List<LocationTelecom> getTelecoms( ) {
		if (telecoms == null) {
			telecoms = new ArrayList<LocationTelecom>();
		}
		return this.telecoms;
	}
	public List<LocationTelecom> addTelecom(LocationTelecom pi) {
		telecoms.add(pi);
		return telecoms; }

	public List<LocationTelecom> removeTelecom(LocationTelecom telecom){
		addresses.remove(telecom); return telecoms; }

	public OrganisationEntity getManagingOrganisation() {
		return managingOrganisation;
	}

	public void setManagingOrganisation(OrganisationEntity managingOrganisation) {
		this.managingOrganisation = managingOrganisation;
	}

	public LocationEntity setType(ConceptEntity type) {
		this.type = type;
		return this;
	}

	public ConceptEntity getType() {
		return type;
	}

	public LocationEntity setPhysicalType(ConceptEntity physicalType) {
		this.physicalType = physicalType;
		return this;
	}

	public ConceptEntity getPhysicalType() {
		return physicalType;
	}

	public Location.LocationStatus getStatus() {
		return status;
	}
	public LocationEntity setStatus(Location.LocationStatus status) {
		this.status = status;
		return this;
	}

	public LocationEntity setPartOf(LocationEntity partOf) {
		this.partOf = partOf;
		return this;
	}

	public void setAddresses(List<LocationAddress> addresses) {
		this.addresses = addresses;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getAltitude() {
		return altitude;
	}

	public void setAltitude(BigDecimal altitude) {
		this.altitude = altitude;
	}

	public LocationEntity getPartOf() {
		return partOf;
	}
}
