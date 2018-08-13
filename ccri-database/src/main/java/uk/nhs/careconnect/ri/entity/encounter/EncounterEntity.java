package uk.nhs.careconnect.ri.entity.encounter;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.*;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.carePlan.CarePlanEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.diagnosticReport.DiagnosticReportEntity;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Encounter",
        indexes = {
                @Index(name = "IDX_ENCOUNTER_DATE", columnList="periodStartDate"),
        })
public class EncounterEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ENCOUNTER_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID", nullable = false,foreignKey= @ForeignKey(name="FK_ENCOUNTER_PATIENT"))
    private PatientEntity patient;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CLASS_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_CLASS_CONCEPT_ID"))
    private ConceptEntity _class;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_TYPE_CONCEPT_ID"))
    private ConceptEntity type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PRIORITY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_PRIORITY_CONCEPT_ID"))
    private ConceptEntity priority;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status", nullable = false)
    Encounter.EncounterStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodStartDate")
    private Date periodStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodEndDate")
    private Date periodEndDate;

    // Now defunct do not use !!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PARTICIPANT_PRACTITIONER_ID")
    private PractitionerEntity participant;

    @OneToMany(mappedBy="encounter", targetEntity = EncounterReason.class)
    Set<EncounterReason> reasons = new HashSet<>();

    @OneToMany(mappedBy="encounter", targetEntity = EncounterDiagnosis.class)
    Set<EncounterDiagnosis> diagnoses = new HashSet<>();

    @OneToMany(mappedBy="encounter", targetEntity = EncounterIdentifier.class)
    Set<EncounterIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="encounter", targetEntity = EncounterEpisode.class)
    Set<EncounterEpisode> episodes = new HashSet<>();

    @OneToMany(mappedBy="encounter", targetEntity = EncounterParticipant.class)
    Set<EncounterParticipant> participants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="LOCATION_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_LOCATION_ID"))
    private LocationEntity location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_ORGANISATION_ID"))
    private OrganisationEntity serviceProvider;

    // For Reverse Includes

    @OneToMany(mappedBy="contextEncounter", targetEntity = ProcedureEntity.class)
    Set<ProcedureEntity> procedureEncounters = new HashSet<>();



    @OneToMany(mappedBy="contextEncounter", targetEntity = ObservationEntity.class)
    Set<ObservationEntity> observationEncounters = new HashSet<>();



    @OneToMany(mappedBy="contextEncounter", targetEntity = ConditionEntity.class)
    Set<ConditionEntity> conditionEncounters = new HashSet<>();

    @OneToMany(mappedBy="contextEncounter", targetEntity = MedicationRequestEntity.class)
    Set<MedicationRequestEntity> medicationRequestEncounters = new HashSet<>();
    // Support for reverse includes

    @OneToMany(mappedBy="contextEncounter", targetEntity = DiagnosticReportEntity.class)
    Set<DiagnosticReportEntity> diagnosticReports = new HashSet<>();

    @OneToMany(mappedBy="contextEncounter", targetEntity = CarePlanEntity.class)
    Set<CarePlanEntity> carePlans = new HashSet<>();
    // Support for reverse includes

    public Long getId() {
        return id;
    }

    public EncounterEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public Set<ConditionEntity> getConditionEncounters() {
        return conditionEncounters;
    }

    public Set<MedicationRequestEntity> getMedicationRequestEncounters() {
        return medicationRequestEncounters;
    }

    public void setMedicationRequestEncounters(Set<MedicationRequestEntity> medicationRequestEncounters) {
        this.medicationRequestEncounters = medicationRequestEncounters;
    }

    public void setConditionEncounters(Set<ConditionEntity> conditionEncounters) {
        this.conditionEncounters = conditionEncounters;
    }

    public Set<ObservationEntity> getObservationEncounters() {
        return observationEncounters;
    }

    public void setObservationEncounters(Set<ObservationEntity> observationEncounters) {
        this.observationEncounters = observationEncounters;
    }

    public Set<EncounterIdentifier> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new HashSet<>();
        }return identifiers;
    }
    public void setIdentifiers(Set<EncounterIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<EncounterEpisode> getEpisodes() {
        return episodes;
    }
    public void setEpisodes(Set<EncounterEpisode> episodes) {
        this.episodes = episodes;
    }

    public Date getPeriodEndDate() {
        return periodEndDate;
    }

    public Date getPeriodStartDate() {
        return periodStartDate;
    }

    public EncounterEntity setPeriodEndDate(Date periodEndDate) {
        this.periodEndDate = periodEndDate;
        return this;
    }

    public EncounterEntity setPeriodStartDate(Date periodStartDate) {
        this.periodStartDate = periodStartDate;
        return this;
    }

    public EncounterEntity  setStatus(Encounter.EncounterStatus status) {
        this.status = status;
        return this;
    }

    public Encounter.EncounterStatus getStatus() {
        return status;
    }

    public ConceptEntity getType() {
        return type;
    }

    public ConceptEntity _getClass() {
        return _class;
    }

    public ConceptEntity getPriority() {
        return priority;
    }

    public OrganisationEntity getServiceProvider() {
        return serviceProvider;
    }

    public Set<EncounterDiagnosis> getDiagnoses() {
        if (diagnoses == null) {
            diagnoses = new HashSet<>();
        }
        return diagnoses;
    }

    public Set<EncounterReason> getReasons() {
        if (reasons == null) {
            reasons = new HashSet<>();
        }
        return reasons;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public EncounterEntity setDiagnoses(Set<EncounterDiagnosis> diagnoses) {
        this.diagnoses = diagnoses;
        return this;
    }

    public EncounterEntity _setClass(ConceptEntity _class) {
        this._class = _class;
        return this;
    }

    public EncounterEntity setLocation(LocationEntity location) {
        this.location = location;
        return this;
    }

    public EncounterEntity setPriority(ConceptEntity priority) {
        this.priority = priority;
        return this;
    }

    public EncounterEntity setServiceProvider(OrganisationEntity serviceProvider) {
        this.serviceProvider = serviceProvider;
        return this;
    }

    public EncounterEntity setReasons(Set<EncounterReason> reasons) {
        this.reasons = reasons;
        return this;
    }

    public EncounterEntity setType(ConceptEntity type) {
        this.type = type;
        return this;
    }

    public ConceptEntity get_class() {
        return _class;
    }

    public void set_class(ConceptEntity _class) {
        this._class = _class;
    }

    public Set<DiagnosticReportEntity> getDiagnosticReports() {
        return diagnosticReports;
    }

    public void setDiagnosticReports(Set<DiagnosticReportEntity> diagnosticReports) {
        this.diagnosticReports = diagnosticReports;
    }

    public Set<CarePlanEntity> getCarePlans() {
        return carePlans;
    }

    public void setCarePlans(Set<CarePlanEntity> carePlans) {
        this.carePlans = carePlans;
    }

    public Set<EncounterParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<EncounterParticipant> participants) {
        this.participants = participants;
    }

    public Set<ProcedureEntity> getProcedureEncounters() {
        return procedureEncounters;
    }

    public void setProcedureEncounters(Set<ProcedureEntity> procedureEncounters) {
        this.procedureEncounters = procedureEncounters;
    }
}
