package uk.nhs.careconnect.ri.database.entity.patient;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;

import javax.persistence.*;


@Entity
@Table(name="PatientTelecom",
		uniqueConstraints= @UniqueConstraint(name="PK_PATIENT_TELECOM", columnNames={"PATIENT_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_PATIENT_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID")
		})
public class PatientTelecom extends BaseContactPoint {

	public PatientTelecom() {

	}

	public PatientTelecom(PatientEntity patientEntity) {
		this.patientEntity = patientEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PATIENT_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PATIENT_TELECOM"))
	private PatientEntity patientEntity;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public PatientEntity getPatient() {
	        return this.patientEntity;
	}
	public void setPatientEntity(PatientEntity organisationEntity) {
	        this.patientEntity = patientEntity;
	}

}
