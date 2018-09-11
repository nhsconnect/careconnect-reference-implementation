package uk.nhs.careconnect.ri.database.entity.healthcareService;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="HealthcareServiceIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_SERVICE_IDENTIFIER", columnNames={"SERVICE_IDENTIFIER_ID"})
		)
public class HealthcareServiceIdentifier extends BaseIdentifier {

	public HealthcareServiceIdentifier() {

	}

	public HealthcareServiceIdentifier(HealthcareServiceEntity service) {
		this.service = service;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "SERVICE_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SERVICE_IDENTIFIER"))
	private HealthcareServiceEntity service;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }


	public HealthcareServiceEntity getService() {
		return service;
	}

	public void setService(HealthcareServiceEntity service) {
		this.service = service;
	}
}
