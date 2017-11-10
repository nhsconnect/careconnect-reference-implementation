package uk.nhs.careconnect.ri.entity.medication;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;

@Entity
@Table(name = "MedicationStatement")
public class MedicationStatementEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MEDICATION_STATEMENT_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_MEDICATION_STATEMENT"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;


    public Long getId() {
        return id;
    }

    public MedicationStatementEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }


}
