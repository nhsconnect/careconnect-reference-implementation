package uk.nhs.careconnect.ri.database.entity.endpoint;

import org.hl7.fhir.dstu3.model.Endpoint;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "EndPoint",indexes =
		{
				@Index(name = "IDX_ENDPOINT_NAME", columnList="ENDPOINT_NAME")

		})
public class EndpointEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ENDPOINT_ID")
	private Long id;

    
    @Column(name = "ENDPOINT_NAME")
	private String name;

	@OneToMany(mappedBy="endpoint", targetEntity=EndpointIdentifier.class)
	private List<EndpointIdentifier> identifiers;

	@Column(name = "STATUS")
	@Enumerated(EnumType.ORDINAL)
	private Endpoint.EndpointStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CONNECTION_TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_CONNECTION_TYPE_CONCEPT"))
	private ConceptEntity connectionType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="MANAGING_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_ENDPOINT_ORGANISATION"))
	private OrganisationEntity managingOrganisation;

	@OneToMany(mappedBy="endpoint", targetEntity=EndpointTelecom.class)
	private List<EndpointTelecom> contacts;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PERIOD_START")
	private Date periodStart;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PERIOD_END")
	private Date periodEnd;

	@OneToMany(mappedBy="endpoint", targetEntity=EndpointPayloadMime.class)
	private List<EndpointPayloadMime> payloadMimes;

	@OneToMany(mappedBy="endpoint", targetEntity=EndpointPayloadType.class)
	private List<EndpointPayloadType> payloadTypes;

	@Column(name = "ADDRESS")
	private String address;

	@Column(name = "HEADERS")
	private String headers;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		if (name != null) return name.trim();
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// EndPoint IDENTIFIERS

	public void setIdentifiers(List<EndpointIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	public List<EndpointIdentifier> getIdentifiers( ) {
		if (identifiers == null) {
			identifiers = new ArrayList<EndpointIdentifier>();
		}
		return this.identifiers;
	}
	public List<EndpointIdentifier> addIdentifier(EndpointIdentifier pi) {
		identifiers.add(pi);
		return identifiers; }

	public List<EndpointIdentifier> removeIdentifier(EndpointIdentifier identifier){
		identifiers.remove(identifiers); return identifiers; }

	// EndPoint Address

	
	public OrganisationEntity getManagingOrganisation() {
		return managingOrganisation;
	}

	public EndpointEntity setManagingOrganisation(OrganisationEntity managingOrganisation) {
		this.managingOrganisation = managingOrganisation;
		return this;
	}

	public Endpoint.EndpointStatus getStatus() {
		return status;
	}

	public EndpointEntity setStatus(Endpoint.EndpointStatus status) {
		this.status = status;
		return this;
	}

	public ConceptEntity getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConceptEntity connectionType) {
		this.connectionType = connectionType;
	}

	public String getAddress() {
		return address;
	}

	public EndpointEntity setAddress(String address) {
		this.address = address;
		return this;
	}

	public List<EndpointTelecom> getContacts() {
		return contacts;
	}

	public void setContacts(List<EndpointTelecom> contacts) {
		this.contacts = contacts;
	}

	public Date getPeriodStart() {
		return periodStart;
	}

	public void setPeriodStart(Date periodStart) {
		this.periodStart = periodStart;
	}

	public Date getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(Date periodEnd) {
		this.periodEnd = periodEnd;
	}

	public List<EndpointPayloadMime> getPayloadMimes() {
		return payloadMimes;
	}

	public void setPayloadMimes(List<EndpointPayloadMime> payloadMimes) {
		this.payloadMimes = payloadMimes;
	}

	public List<EndpointPayloadType> getPayloadTypes() {
		return payloadTypes;
	}

	public void setPayloadTypes(List<EndpointPayloadType> payloadTypes) {
		this.payloadTypes = payloadTypes;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}
}
