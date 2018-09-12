package uk.nhs.careconnect.ri.database.entity.patient;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="PatientIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PATIENT_IDENTIFIER", columnNames={"PATIENT_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_PATIENT_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")
		})
public class PatientIdentifier extends BaseIdentifier {
	
	public PatientIdentifier() {
	}
    public PatientIdentifier(PatientEntity patientEntity) {
		this.patientEntity = patientEntity;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PATIENT_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PATIENT_IDENTIFIER"))
    private PatientEntity patientEntity;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public PatientEntity getPatient() {
	        return this.patientEntity;
	}
	public void setPatient(PatientEntity patientEntity) {
	        this.patientEntity = patientEntity;
	}




}
