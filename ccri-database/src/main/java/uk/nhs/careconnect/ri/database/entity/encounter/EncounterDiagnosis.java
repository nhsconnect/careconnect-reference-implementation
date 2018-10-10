package uk.nhs.careconnect.ri.database.entity.encounter;

import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;

import javax.persistence.*;

@Entity
@Table(name="EncounterDiagnosis1", uniqueConstraints= @UniqueConstraint(name="PK_ENCOUNTER_DIAGNOSIS", columnNames={"ENCOUNTER_DIAGNOSIS_ID"})
        ,indexes = {
        @Index(name="IDX_ENCOUNTER_CONDITION1", columnList = "DIAGNOSIS_CONDITION_ID"),
        @Index(name="IDX_ENCOUNTER_DIAGNOSIS1_ENCOUNTER_ID", columnList = "ENCOUNTER_ID")
}
)
public class EncounterDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ENCOUNTER_DIAGNOSIS_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_DIAGNOSIS_ENCOUNTER_ID"))

    private EncounterEntity encounter;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "DIAGNOSIS_CONDITION_ID", nullable = false, foreignKey= @ForeignKey(name="FK_ENCOUNTER_DIAGNOSIS_CONDITION_ID"))

    private ConditionEntity condition;


    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public EncounterDiagnosis setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
        return this;
    }

    public EncounterDiagnosis setCondition(ConditionEntity condition) {
        this.condition = condition;
        return this;
    }

    public ConditionEntity getCondition() {
        return condition;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }
}
