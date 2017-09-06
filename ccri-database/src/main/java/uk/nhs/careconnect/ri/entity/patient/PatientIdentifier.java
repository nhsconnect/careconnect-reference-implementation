package uk.nhs.careconnect.ri.entity.patient;

import uk.nhs.careconnect.ri.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@Entity
@Table(name="PatientIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PATIENT_IDENTIFIER", columnNames={"PATIENT_IDENTIFIER_ID"}))
public class PatientIdentifier extends BaseIdentifier {
	
	public PatientIdentifier() {
		
	}
	
	public PatientIdentifier(PatientEntity patientEntity) {
		this.patientEntity = patientEntity;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PATIENT_IDENTIFIER_ID")
	public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }
	private Integer identifierId;
	
	private PatientEntity patientEntity;
    @ManyToOne
	@JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PATIENT_IDENTIFIER"))
	public PatientEntity getPatientEntity() {
	        return this.patientEntity;
	}
	public void setPatientEntity(PatientEntity patientEntity) {
	        this.patientEntity = patientEntity;
	}

    private SystemEntity systemEntity;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SYSTEM_ID",foreignKey= @ForeignKey(name="FK_SYSTEM_PATIENT_IDENTIFIER"))
    public SystemEntity getSystem() {
        return this.systemEntity;
    }
    public void setSystem(SystemEntity systemEntity) {
        this.systemEntity = systemEntity;
    }

}
