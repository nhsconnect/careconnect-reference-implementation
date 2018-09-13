package uk.nhs.careconnect.ri.database.entity.patient;


import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;

import javax.persistence.*;

@Entity
@Table(name = "PatientAddress")
public class PatientAddress extends BaseAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PATIENT_ADDRESS_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ADDRESS_ADDRESS"))
    private AddressEntity address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ADDRESS_PATIENT"))
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

    @Override
    public AddressEntity getAddress() {
        return this.address;
    }
    @Override
    public AddressEntity setAddress(AddressEntity addressEntity) {
        this.address = addressEntity;
        return this.address;
    }

}
