package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@Entity
@Table(name="PractitionerIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PRACTITIONER_IDENTIFIER", columnNames={"PRACTITIONER_IDENTIFIER_ID"}))
public class PractitionerIdentifier extends BaseIdentifier {

	public PractitionerIdentifier() {

	}

	public PractitionerIdentifier(PractitionerEntity practitionerEntity) {
		this.practitionerEntity = practitionerEntity;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PRACTITIONER_IDENTIFIER_ID")
	public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }
	private Integer identifierId;
	
	private PractitionerEntity practitionerEntity;

	@ManyToOne
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_PRACTITIONER_IDENTIFIER"))
	public PractitionerEntity getPractitioner() {
	        return this.practitionerEntity;
	}
	public void setPractitioner(PractitionerEntity practitionerEntity) {
	        this.practitionerEntity = practitionerEntity;
	}

    private SystemEntity systemEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SYSTEM_ID",foreignKey= @ForeignKey(name="FK_SYSTEM_PRACTITIONER_IDENTIFIER"))
    public SystemEntity getSystem() {
        return this.systemEntity;
    }
    public void setSystem(SystemEntity systemEntity) {
        this.systemEntity = systemEntity;
    }
}
