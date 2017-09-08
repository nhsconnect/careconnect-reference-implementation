package uk.nhs.careconnect.ri.entity.location;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Location")
public class LocationEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="LOCATION_ID")
	private Long id;

    
    @Column(name = "name")
	private String name;

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
	@OneToMany(mappedBy="locationEntity", targetEntity=LocationIdentifier.class)
	private List<LocationIdentifier> identifiers;
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
	@OneToMany(mappedBy="locationEntity", targetEntity=LocationAddress.class)
	private List<LocationAddress> addresses;
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
	@OneToMany(mappedBy="locationEntity", targetEntity=LocationTelecom.class)
	private List<LocationTelecom> telecoms;
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
}
