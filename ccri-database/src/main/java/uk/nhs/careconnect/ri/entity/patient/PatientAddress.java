package uk.nhs.careconnect.ri.entity.patient;


import uk.nhs.careconnect.ri.entity.AddressEntity;

import javax.persistence.*;

@Entity
@Table(name = "PatientAddress")
public class PatientAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PATIENT_ADDRESS_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

    @ManyToOne
    @JoinColumn(name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PATIENT_ADDRESS"))
    private PatientEntity patientEntity;

    public Long getPID()
    {
        return this.myId;
    }

    public PatientEntity getPatientEntity() {
        return this.patientEntity;
    }
    public void setPatientEntity(PatientEntity patientEntity) {
        this.patientEntity = patientEntity;
    }


    public AddressEntity getAddress() {
        return this.address;
    }
    public void setAddress(AddressEntity addressEntity) { this.address = addressEntity; }



}