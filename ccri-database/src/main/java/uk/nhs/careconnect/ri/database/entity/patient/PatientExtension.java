package uk.nhs.careconnect.ri.database.entity.patient;

import uk.nhs.careconnect.ri.database.entity.BaseExtension;
import javax.persistence.*;

@Entity
@Table(name = "PatientExtension")
public class PatientExtension extends BaseExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PATIENT_EXTENSION_ID")
    private Long extensionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_EXTENSION_PATIENT"))
    private PatientEntity patientEntity;


    public PatientEntity getPatientEntity() {
        return this.patientEntity;
    }
    public void setPatientEntity(PatientEntity patientEntity) {
        this.patientEntity = patientEntity;
    }

}
