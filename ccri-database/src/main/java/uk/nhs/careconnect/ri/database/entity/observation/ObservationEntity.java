package uk.nhs.careconnect.ri.database.entity.observation;

import org.hl7.fhir.dstu3.model.Observation;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Observation", indexes = {

        @Index(name="IDX_OBSERVATION_DATE", columnList = "effectiveDateTime"),
        @Index(name="IDX_PARENT_OBSERVATION", columnList = "PARENT_OBSERVATION_ID")

})
public class ObservationEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 1024;

    public enum ObservationType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="OBSERVATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_PATIENT_ID"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CODE_CONCEPT_ID",nullable = false,foreignKey= @ForeignKey(name="FK_OBSERVATION_CODE_CONCEPT_ID"))
    private ConceptEntity code;

    // The parent should not be null but child observations don't have a status.
    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Observation.ObservationStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effectiveDateTime")
    private Date effectiveDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued")
    private Date issued;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name="PARENT_OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_PARENT_OBSERVATION_ID"))
    private ObservationEntity parentObservation;

    @Column(name="valueQuantity")
    private BigDecimal valueQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="valueUnitOfMeasure_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_valueUnitOfMeasure_CONCEPT_ID"))
    private ConceptEntity valueUnitOfMeasure;

    @Enumerated(EnumType.ORDINAL)
    private ObservationType observationType;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Column(name="COMMENT",length = MAX_DESC_LENGTH,nullable = true)
    private String comments;


    @OneToMany(mappedBy="observation", targetEntity=ObservationCategory.class)
    private Set<ObservationCategory> categories = new HashSet<>();

    @OneToMany(mappedBy="observation", targetEntity=ObservationIdentifier.class)
    private Set<ObservationIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="observation", targetEntity=ObservationPerformer.class)
    private Set<ObservationPerformer> performers = new HashSet<>();

    @OneToMany(mappedBy="observation", targetEntity = ObservationRange.class)
    private Set<ObservationRange> ranges = new HashSet<>();

    @OneToMany(mappedBy="parentObservation", targetEntity = ObservationEntity.class)
    private Set<ObservationEntity> components = new HashSet<>();

    @OneToMany(mappedBy="observation", targetEntity = ObservationRelated.class)
    private Set<ObservationRelated> relatedResources = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="BODY_SITE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_BODY_SITE_CONCEPT_ID"))
    private ConceptEntity bodySite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="METHOD_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_METHOD_CONCEPT_ID"))
    private ConceptEntity method;

    @Column(name="valueString")
    private BigDecimal valueString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="INTERPRETATION_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_INTERPRETATION_CONCEPT_ID"))
    private ConceptEntity interpretation;



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

    public Set<ObservationCategory> getCategories() {
        if (categories == null) { categories = new HashSet<ObservationCategory>(); }
        return categories;
    }

    public Set<ObservationIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<ObservationIdentifier>(); }
        return identifiers;
    }

    public Set<ObservationPerformer> getPerformers() {
        if (performers == null) { performers = new HashSet<ObservationPerformer>(); }
        return performers;
    }

    public Date getIssued() {
        return issued;
    }

    public ObservationEntity setIssued(Date issued) {
        this.issued = issued;
        return this;
    }

    public ObservationEntity setCategories(Set<ObservationCategory> categories) {
        this.categories = categories;
        return this;
    }

    public Observation.ObservationStatus getStatus() {
        return status;
    }

    public ObservationEntity setIdentifiers(Set<ObservationIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public ObservationEntity setPerformers(Set<ObservationPerformer> performers) {
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

    public Set<ObservationEntity> getComponents() {
        return components;
    }

    public ObservationEntity setComponents(Set<ObservationEntity> components) {
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

    public EncounterEntity getContext() {
        return contextEncounter;
    }

    public ObservationEntity setContext(EncounterEntity context) {
        this.contextEncounter = context;
        return this;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public ObservationEntity setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
        return this;
    }

    public Set<ObservationRange> getRanges() {
        return ranges;
    }

    public ObservationEntity setRanges(Set<ObservationRange> ranges) {
        this.ranges = ranges;
        return this;
    }

    public ConceptEntity getInterpretation() {
        return interpretation;
    }

    public ObservationEntity setInterpretation(ConceptEntity interpretation) {
        this.interpretation = interpretation;
        return this;
    }

    public Set<ObservationRelated> getRelatedResources() {
        return relatedResources;
    }

    public void setRelatedResources(Set<ObservationRelated> relatedResources) {
        this.relatedResources = relatedResources;
    }
}
