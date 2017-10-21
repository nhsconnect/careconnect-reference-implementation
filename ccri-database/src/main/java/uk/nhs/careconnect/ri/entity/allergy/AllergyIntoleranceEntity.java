package uk.nhs.careconnect.ri.entity.allergy;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;

@Entity
@Table(name = "AllergyIntolerance")
public class AllergyIntoleranceEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ALLERGY_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ALLERGY"))
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "SUBSTANCE_CODE_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_SUBSTANCE"))
    private ConceptEntity substance;


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
}
