package uk.nhs.careconnect.ri.entity.observation;

import org.hl7.fhir.dstu3.model.Observation;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Observation", indexes = {
        @Index(name="IDX_OBSERVATION_CODE", columnList = "code"),
        @Index(name="IDX_OBSERVATION_DATE", columnList = "effectiveDateTime")
})
public class ObservationEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="OBSERVATION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_OBSERVATION"))
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name="code")
    private ConceptEntity code;

    @Enumerated(EnumType.ORDINAL)
    private Observation.ObservationStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effectiveDateTime")
    private Date effectiveDateTime;

    @ManyToOne
    @JoinColumn(name="parentObservation")
    private ObservationEntity parentObservation;

    @OneToMany(mappedBy="observation", targetEntity=ObservationCategory.class)
    private List<ObservationCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy="observation", targetEntity=ObservationIdentifier.class)
    private List<ObservationIdentifier> identifiers = new ArrayList<>();

    @OneToMany(mappedBy="observation", targetEntity=ObservationPerformer.class)
    private List<ObservationPerformer> performers = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public ObservationEntity getParentObservation() {
        return parentObservation;
    }

    public void setCode(ConceptEntity code) {
        this.code = code;
    }

    public Date getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public void setEffectiveDateTime(Date effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }

    public void setParentObservation(ObservationEntity parentObservation) {
        this.parentObservation = parentObservation;
    }

    public List<ObservationCategory> getCategories() {
        if (categories == null) { categories = new ArrayList<ObservationCategory>(); }
        return categories;
    }

    public List<ObservationIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new ArrayList<ObservationIdentifier>(); }
        return identifiers;
    }

    public List<ObservationPerformer> getPerformers() {
        if (performers == null) { performers = new ArrayList<ObservationPerformer>(); }
        return performers;
    }

    public void setCategories(List<ObservationCategory> categories) {
        this.categories = categories;
    }

    public Observation.ObservationStatus getStatus() {
        return status;
    }

    public void setIdentifiers(List<ObservationIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public void setPerformers(List<ObservationPerformer> performers) {
        this.performers = performers;
    }

    public void setStatus(Observation.ObservationStatus status) {
        this.status = status;
    }
}
