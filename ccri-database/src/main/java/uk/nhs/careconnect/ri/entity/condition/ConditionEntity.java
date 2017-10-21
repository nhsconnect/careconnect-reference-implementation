package uk.nhs.careconnect.ri.entity.condition;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;

@Entity
@Table(name = "Condition")
public class ConditionEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CONDITION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_CONDITION"))
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_CONDITION_ENCOUNTER"))
    private EncounterEntity encounter;

    @ManyToOne
    @JoinColumn (name = "CODE_ID",foreignKey= @ForeignKey(name="FK_CONDITION_CODE"))
    private ConceptEntity code;


    public Long getId() {
        return id;
    }

    public ConditionEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ConditionEntity setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
        return this;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public ConditionEntity setCode(ConceptEntity code) {
        this.code = code;
        return this;
    }
}
