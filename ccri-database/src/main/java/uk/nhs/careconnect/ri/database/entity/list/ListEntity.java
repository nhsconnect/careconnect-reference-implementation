package uk.nhs.careconnect.ri.database.entity.list;

import org.hl7.fhir.dstu3.model.ListResource;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "List",
        indexes = {

        })
public class ListEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum ListType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="LIST_ID")
    private Long id;

    @OneToMany(mappedBy="list", targetEntity=ListIdentifier.class)
    private Set<ListIdentifier> identifiers = new HashSet<>();



    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private ListResource.ListStatus status;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="MODE_ID")
    private ListResource.ListMode mode;

    @Column(name="TITLE",length = MAX_DESC_LENGTH,nullable = true)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_LIST_CODE_CONCEPT_ID"))
    private ConceptEntity code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_LIST_PATIENT_ID"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_LIST_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATETIME")
    private Date dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "SOURCE_PATIENT_ID",foreignKey= @ForeignKey(name="FK_LIST_SOURCE_PATIENT_ID"))
    private PatientEntity sourcePatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "SOURCE_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_LIST_SOURCE_PRACTITIONER_ID"))
    private PractitionerEntity sourcePractitioner;

    @Column(name="NOTE",length = MAX_DESC_LENGTH,nullable = true)
    private String note;

    @OneToMany(mappedBy="list", targetEntity=ListItem.class)
    private Set<ListItem> items = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EMPTY_REASON_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_LIST_EMPTY_REASON_CONCEPT_ID"))
    private ConceptEntity emptyReason;

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

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }



    public PatientEntity getSourcePatient() {
        return sourcePatient;
    }

    public void setSourcePatient(PatientEntity sourcePatient) {
        this.sourcePatient = sourcePatient;
    }

    public PractitionerEntity getSourcePractitioner() {
        return sourcePractitioner;
    }

    public void setSourcePractitioner(PractitionerEntity sourcePractitioner) {
        this.sourcePractitioner = sourcePractitioner;
    }

    public Set<ListIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<ListIdentifier>(); }
        return identifiers;
    }

   

    public ListEntity setIdentifiers(Set<ListIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


   

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public ListResource.ListStatus getStatus() {
        return status;
    }

    public void setStatus(ListResource.ListStatus status) {
        this.status = status;
    }

    public ListResource.ListMode getMode() {
        return mode;
    }

    public void setMode(ListResource.ListMode mode) {
        this.mode = mode;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Set<ListItem> getItems() {
        return items;
    }

    public void setItems(Set<ListItem> items) {
        this.items = items;
    }

    public ConceptEntity getEmptyReason() {
        return emptyReason;
    }

    public void setEmptyReason(ConceptEntity emptyReason) {
        this.emptyReason = emptyReason;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public void setCode(ConceptEntity code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
