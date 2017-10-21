package uk.nhs.careconnect.ri.entity.procedure;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;

@Entity
@Table(name = "Procedure_")
public class ProcedureEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PROCEDURE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PROCEDURE"))
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_ENCOUNTER"))
    private EncounterEntity encounter;

    @ManyToOne
    @JoinColumn (name = "CODE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_CODE"))
    private ConceptEntity code;

    public Long getId() {
        return id;
    }

    public ProcedureEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public ProcedureEntity setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
        return this;
    }

    public ProcedureEntity setCode(ConceptEntity code) {
        this.code = code;
        return this;
    }
}
