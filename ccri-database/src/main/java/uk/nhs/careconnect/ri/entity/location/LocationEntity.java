package uk.nhs.careconnect.ri.entity.location;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.Location;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Location",indexes =
		{
				@Index(name = "IDX_LOCATION_NAME", columnList="name")

		})
public class LocationEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="LOCATION_ID")
	private Long id;

    
    @Column(name = "name")
	private String name;

	@OneToMany(mappedBy="locationEntity", targetEntity=LocationIdentifier.class)
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<LocationIdentifier> identifiers;

	@OneToMany(mappedBy="locationEntity", targetEntity=LocationAddress.class)
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<LocationAddress> addresses;

	@OneToMany(mappedBy="locationEntity", targetEntity=LocationTelecom.class)
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<LocationTelecom> telecoms;

	@ManyToOne
	@JoinColumn(name="managingOrganisation",foreignKey= @ForeignKey(name="FK_LOCATION_ORGANISATION"))
	@LazyCollection(LazyCollectionOption.TRUE)
	private OrganisationEntity managingOrganisation;

	@ManyToOne
	@JoinColumn(name="type",foreignKey= @ForeignKey(name="FK_LOCATION_TYPE_CONCEPT"))
	private ConceptEntity type;

	@ManyToOne
	@JoinColumn(name="physicalType",foreignKey= @ForeignKey(name="FK_LOCATION_PHYSCIAL_TYPE_CONCEPT"))
	private ConceptEntity physicalType;

	@ManyToOne
	@JoinColumn(name="PART_OF_LOCATION_ID",foreignKey= @ForeignKey(name="FK_LOCATION_PARTOF_LOCATION"))
	@LazyCollection(LazyCollectionOption.TRUE)
	private LocationEntity partOf;

	@Enumerated(EnumType.ORDINAL)
	Location.LocationStatus status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
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

	public void setType(ConceptEntity type) {
		this.type = type;
	}

	public ConceptEntity getType() {
		return type;
	}

	public void setPhysicalType(ConceptEntity physicalType) {
		this.physicalType = physicalType;
	}

	public ConceptEntity getPhysicalType() {
		return physicalType;
	}

	public Location.LocationStatus getStatus() {
		return status;
	}
	public void setStatus(Location.LocationStatus status) {
		this.status = status;
	}
}
