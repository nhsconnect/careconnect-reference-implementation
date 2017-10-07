package uk.nhs.careconnect.ri.entity.observation;

import org.hl7.fhir.dstu3.model.Observation;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Observation", indexes = {
        @Index(name="IDX_OBSERVATION_CODE", columnList = "code"),
        @Index(name="IDX_OBSERVATION_DATE", columnList = "effectiveDateTime")
})
public class ObservationEntity extends BaseResource {

    public enum ObservationType  { component, valueQuantity }

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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued")
    private Date issued;

    @ManyToOne
    @JoinColumn(name="parentObservation")
    private ObservationEntity parentObservation;

    @Column(name="valueQuantity")
    private BigDecimal valueQuantity;

    @ManyToOne
    @JoinColumn(name="valueUnitOfMeasure")
    private ConceptEntity valueUnitOfMeasure;

    @Enumerated(EnumType.ORDINAL)
    private ObservationType observationType;

    @OneToMany(mappedBy="observation", targetEntity=ObservationCategory.class)
    private List<ObservationCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy="observation", targetEntity=ObservationIdentifier.class)
    private List<ObservationIdentifier> identifiers = new ArrayList<>();

    @OneToMany(mappedBy="observation", targetEntity=ObservationPerformer.class)
    private List<ObservationPerformer> performers = new ArrayList<>();

    @OneToMany(mappedBy="parentObservation", targetEntity = ObservationEntity.class)
    private List<ObservationEntity> components = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="bodySite")
    private ConceptEntity bodySite;

    @ManyToOne
    @JoinColumn(name="method")
    private ConceptEntity method;

    @Column(name="valueString")
    private BigDecimal valueString;

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

    public ObservationType getObservationType() {
        return observationType;
    }

    public ObservationEntity setObservationType(ObservationType observationType) {
        this.observationType = observationType;
        return this;
    }

    public void setEffectiveDateTime(Date effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }

    public ObservationEntity setParentObservation(ObservationEntity parentObservation) {
        this.parentObservation = parentObservation;
        return this;
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

    public Date getIssued() {
        return issued;
    }

    public ObservationEntity setIssued(Date issued) {
        this.issued = issued;
        return this;
    }

    public ObservationEntity setCategories(List<ObservationCategory> categories) {
        this.categories = categories;
        return this;
    }

    public Observation.ObservationStatus getStatus() {
        return status;
    }

    public ObservationEntity setIdentifiers(List<ObservationIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public ObservationEntity setPerformers(List<ObservationPerformer> performers) {
        this.performers = performers;
        return this;
    }

    public ObservationEntity setStatus(Observation.ObservationStatus status) {
        this.status = status;
        return this;
    }

    public BigDecimal getValueQuantity() {
        return valueQuantity;
    }

    public ConceptEntity getValueUnitOfMeasure() {
        return valueUnitOfMeasure;
    }

    public List<ObservationEntity> getComponents() {
        return components;
    }

    public ObservationEntity setComponents(List<ObservationEntity> components) {
        this.components = components;
        return this;
    }

    public ObservationEntity setValueQuantity(BigDecimal valueQuantity) {
        this.valueQuantity = valueQuantity;
        return this;
    }

    public ObservationEntity setValueUnitOfMeasure(ConceptEntity valueUnitOfMeasure) {
        this.valueUnitOfMeasure = valueUnitOfMeasure;
        return this;
    }

    public ConceptEntity getBodySite() {
        return bodySite;
    }

    public ConceptEntity getMethod() {
        return method;
    }

    public ObservationEntity setBodySite(ConceptEntity bodySite) {
        this.bodySite = bodySite;
        return this;
    }

    public ObservationEntity setMethod(ConceptEntity method) {
        this.method = method;
        return this;
    }
    public BigDecimal getValueString() {
        return valueString;
    }
    public ObservationEntity setValueString(BigDecimal valueString) {
        this.valueString = valueString;
        return this;
    }
}
