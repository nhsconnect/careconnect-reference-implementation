package uk.nhs.careconnect.ri.entity.patient;


import uk.nhs.careconnect.ri.entity.BaseHumanName;

import javax.persistence.*;

@Entity
@Table(name = "PatientName")
public class PatientName extends BaseHumanName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PATIENT_NAME_ID")
    private Long myId;


    @ManyToOne
    @JoinColumn(name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PATIENT_NAME"))
    private PatientEntity patientEntity;

    public Long getId()
    {
        return this.myId;
    }

    public PatientEntity getPatientEntity() {
        return this.patientEntity;
    }
    public void setPatientEntity(PatientEntity patientEntity) {
        this.patientEntity = patientEntity;
    }


}