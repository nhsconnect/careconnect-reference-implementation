package uk.nhs.careconnect.ri.database.entity.patient;


import uk.nhs.careconnect.ri.database.entity.BaseHumanName;

import javax.persistence.*;

@Entity
@Table(name = "PatientName",indexes =
        {
            @Index(name = "IDX_PATIENT_FAMILY", columnList="family_name")
                , @Index(name = "IDX_PATIENT_GIVEN", columnList="given_name")
        })
public class PatientName extends BaseHumanName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PATIENT_NAME_ID")
    private Long myId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_NAME_PATIENT_ID"))
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
