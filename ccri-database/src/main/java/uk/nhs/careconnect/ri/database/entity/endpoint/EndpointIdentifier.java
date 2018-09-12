package uk.nhs.careconnect.ri.database.entity.endpoint;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="EndpointIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_ENDPOINT_IDENTIFIER", columnNames={"ENDPOINT_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_ENDPOINT_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_ENDPOINT_IDENTIFER_ENDPOINT_ID", columnList="ENDPOINT_ID")


		})
public class EndpointIdentifier extends BaseIdentifier {

	public EndpointIdentifier() {

	}

	public EndpointIdentifier(EndpointEntity endpointEntity) {
		this.endpointEntity = endpointEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ENDPOINT_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ENDPOINT_ID",foreignKey= @ForeignKey(name="FK_ENDPOINT_ENDPOINT_IDENTIFIER"))
	private EndpointEntity endpointEntity;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public EndpointEntity getEndPoint() {
	        return this.endpointEntity;
	}

	public void setEndpoint(EndpointEntity endpointEntity) {
	        this.endpointEntity = endpointEntity;
	}




}
