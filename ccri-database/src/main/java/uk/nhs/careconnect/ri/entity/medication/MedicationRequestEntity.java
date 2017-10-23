package uk.nhs.careconnect.ri.entity.medication;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MedicationRequest")
public class MedicationRequestEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRESCRIPTION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PRESCRIPTION"))
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn (name = "MEDICATION_CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_MEDICATION_CODE"))
    private ConceptEntity medicationCode;

    @ManyToOne
    @JoinColumn(name="ENCOUNTER_ID")
    private EncounterEntity contextEncounter;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "writtenDate")
    private Date writtenDate;

    @ManyToOne
    @JoinColumn(name="STATUS_CONCEPT_ID")
    private ConceptEntity status;

    public Long getId() {
        return id;
    }

    public MedicationRequestEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ConceptEntity getMedicationCode() {
        return medicationCode;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public MedicationRequestEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }

    public MedicationRequestEntity setMedicationCode(ConceptEntity medicationCode) {
        this.medicationCode = medicationCode;
        return this;
    }

    public ConceptEntity getStatus() {
        return status;
    }

    public MedicationRequestEntity setStatus(ConceptEntity status) {
        this.status = status;
        return this;
    }

    public Date getWrittenDate() {
        return writtenDate;
    }

    public MedicationRequestEntity setWrittenDate(Date writtenDate) {
        this.writtenDate = writtenDate;
        return this;
    }
}
