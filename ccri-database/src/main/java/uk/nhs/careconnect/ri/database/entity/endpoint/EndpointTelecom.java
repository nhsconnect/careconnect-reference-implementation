package uk.nhs.careconnect.ri.database.entity.endpoint;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointEntity;

import javax.persistence.*;


@Entity
@Table(name="EndpointTelecom", uniqueConstraints= @UniqueConstraint(name="PK_ENDPOINT_TELECOM", columnNames={"ENDPOINT_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_ENDPOINT_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_ENDPOINT_TELECOM_ENDPOINT_ID", columnList="ENDPOINT_ID")
		})
public class EndpointTelecom extends BaseContactPoint {

	public EndpointTelecom() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ENDPOINT_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ENDPOINT_ID",foreignKey= @ForeignKey(name="FK_ENDPOINT_TELECOM_ENDPOINT_ID"))
	private EndpointEntity endpoint;

	public Long getTelecomId() {
		return telecomId;
	}

	public void setTelecomId(Long telecomId) {
		this.telecomId = telecomId;
	}

	public EndpointEntity getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(EndpointEntity endpoint) {
		this.endpoint = endpoint;
	}
}
