package uk.nhs.careconnect.ri.entity.allergy;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "AllergyIntolerance")
public class AllergyIntoleranceEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ALLERGY_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ALLERGY"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "SUBSTANCE_CODE_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_SUBSTANCE_CONCEPT"))
    private ConceptEntity substance;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "recordedDate")
    private Date recordedDate;

    @ManyToOne
    @JoinColumn(name="status_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_STATUS_CONCEPT"))
    private ConceptEntity status;


    public Long getId() {
        return id;
    }

    public AllergyIntoleranceEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ConceptEntity getSubstance() {
        return substance;
    }

    public AllergyIntoleranceEntity setSubstance(ConceptEntity substance) {
        this.substance = substance;
        return this;
    }

    public AllergyIntoleranceEntity setStatus(ConceptEntity status) {
        this.status = status;
        return this;
    }

    public ConceptEntity getStatus() {
        return status;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public AllergyIntoleranceEntity setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
        return this;
    }
}
