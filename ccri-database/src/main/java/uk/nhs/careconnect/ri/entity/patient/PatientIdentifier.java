package uk.nhs.careconnect.ri.entity.patient;

import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@Entity
@Table(name="PatientIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PATIENT_IDENTIFIER", columnNames={"PATIENT_IDENTIFIER_ID"}))
public class PatientIdentifier {
	
	public PatientIdentifier() {
	}
    public PatientIdentifier(PatientEntity patientEntity) {
		this.patientEntity = patientEntity;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PATIENT_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PATIENT_IDENTIFIER"))
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SYSTEM_ID",foreignKey= @ForeignKey(name="FK_SYSTEM_PATIENT_IDENTIFIER"))
    private SystemEntity system;

    @Column(name = "Value")
    private String value;

    @Column(name = "ListOrder")
    private Long order;

    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }

	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public PatientEntity getPatient() {
	        return this.patientEntity;
	}
	public void setPatient(PatientEntity patientEntity) {
	        this.patientEntity = patientEntity;
	}


    public SystemEntity getSystem() {
        return this.system;
    }
    public void setSystem(SystemEntity systemEntity) {
        this.system = systemEntity;
    }

}
