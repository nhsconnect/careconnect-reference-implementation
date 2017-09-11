package uk.nhs.careconnect.ri.entity.patient;


import org.hl7.fhir.instance.model.Address;
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

    @Enumerated(EnumType.ORDINAL)
    Address.AddressUse addressUse;

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


    public AddressEntity getAddress() {
        return this.address;
    }
    public void setAddress(AddressEntity addressEntity) { this.address = addressEntity; }

    public Address.AddressUse getAddressUse() {
        return addressUse;
    }
    public void setAddressUse(Address.AddressUse addressUse) {
        this.addressUse = addressUse;
    }
}
