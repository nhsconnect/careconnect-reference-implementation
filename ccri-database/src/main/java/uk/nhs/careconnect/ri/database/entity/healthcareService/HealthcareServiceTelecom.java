package uk.nhs.careconnect.ri.database.entity.healthcareService;
import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;
import javax.persistence.*;


@Entity
@Table(name="HealthcareServiceTelecom", uniqueConstraints= @UniqueConstraint(name="PK_SERVICE_TELECOM", columnNames={"SERVICE_TELECOM_ID"}))
public class HealthcareServiceTelecom extends BaseContactPoint {

	public HealthcareServiceTelecom() {

	}

	public HealthcareServiceTelecom(HealthcareServiceEntity serviceEntity) {
		this.service = serviceEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "SERVICE_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SERVICE_TELECOM"))
	private HealthcareServiceEntity service;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public HealthcareServiceEntity getHealthcareService() {
	        return this.service;
	}
	public void setHealthcareService(HealthcareServiceEntity serviceEntity) {
	        this.service = serviceEntity;
	}

}
