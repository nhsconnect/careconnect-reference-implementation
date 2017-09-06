package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@Entity
@Table(name="PractitionerIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PRACTITIONER_IDENTIFIER", columnNames={"PRACTITIONER_IDENTIFIER_ID"}))
public class PractitionerIdentifier {

	public PractitionerIdentifier() {

	}

	public PractitionerIdentifier(PractitionerEntity practitionerEntity) {
		this.practitionerEntity = practitionerEntity;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PRACTITIONER_IDENTIFIER_ID")
	private Integer identifierId;

	@ManyToOne
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_PRACTITIONER_IDENTIFIER"))
	private PractitionerEntity practitionerEntity;

	@ManyToOne
	@JoinColumn(name = "SYSTEM_ID",foreignKey= @ForeignKey(name="FK_SYSTEM_PRACTITIONER_IDENTIFIER"))
	private SystemEntity systemEntity;

    @Column(name = "Value")
    private String value;

    @Column(name = "ListOrder")
    private Integer order;



    public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }

	public PractitionerEntity getPractitioner() {
	        return this.practitionerEntity;
	}
	public void setPractitioner(PractitionerEntity practitionerEntity) {
	        this.practitionerEntity = practitionerEntity;
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
