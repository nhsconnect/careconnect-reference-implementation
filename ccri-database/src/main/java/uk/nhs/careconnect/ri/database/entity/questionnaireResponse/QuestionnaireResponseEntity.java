package uk.nhs.careconnect.ri.database.entity.questionnaireResponse;

import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "QuestionnaireResponse",
        indexes = {

        })
public class QuestionnaireResponseEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum QuestionnaireResponseType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="FORM_ID")
    private Long id;

    @OneToMany(mappedBy="form", targetEntity=QuestionnaireResponseIdentifier.class)
    private Set<QuestionnaireResponseIdentifier> identifiers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "QUESTIONNAIRE_ID",foreignKey= @ForeignKey(name="FK_FORM_QUESTIONNAIRE_ID"))
    private QuestionnaireEntity questionnaire;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private QuestionnaireResponse.QuestionnaireResponseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "BASEDON_CAREPLAN_ID",foreignKey= @ForeignKey(name="FK_FORM_CAREPLAN_ID"))
    private CarePlanEntity carePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_FORM_PATIENT_ID"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_FORM_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EPISODE_ID",foreignKey= @ForeignKey(name="FK_FORM_EPISODE_ID"))
    private EpisodeOfCareEntity contextEpisodeOfCare;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "AUTHORED_DATETIME")
    private Date authoredDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "SOURCE_PATIENT_ID",foreignKey= @ForeignKey(name="FK_FORM_SOURCE_PATIENT_ID"))
    private PatientEntity sourcePatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "SOURCE_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_FORM_SOURCE_PRACTITIONER_ID"))
    private PractitionerEntity sourcePractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "AUTHOR_PATIENT_ID",foreignKey= @ForeignKey(name="FK_FORM_AUTHOR_PATIENT_ID"))
    private PatientEntity authorPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "AUTHOR_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_FORM_AUTHOR_PRACTITIONER_ID"))
    private PractitionerEntity authorPractitioner;

    @OneToMany(mappedBy="form", targetEntity=QuestionnaireResponseItem.class)
    private Set<QuestionnaireResponseItem> items = new HashSet<>();

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

    public EpisodeOfCareEntity getContextEpisodeOfCare() {
        return contextEpisodeOfCare;
    }

    public void setContextEpisodeOfCare(EpisodeOfCareEntity contextEpisodeOfCare) {
        this.contextEpisodeOfCare = contextEpisodeOfCare;
    }

    public Date getAuthoredDateTime() {
        return authoredDateTime;
    }

    public void setAuthoredDateTime(Date authoredDateTime) {
        this.authoredDateTime = authoredDateTime;
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

    public Set<QuestionnaireResponseIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<QuestionnaireResponseIdentifier>(); }
        return identifiers;
    }

    public QuestionnaireResponse.QuestionnaireResponseStatus getStatus() {
        return status;
    }

    public QuestionnaireResponseEntity setIdentifiers(Set<QuestionnaireResponseIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


    public QuestionnaireResponseEntity setStatus(QuestionnaireResponse.QuestionnaireResponseStatus status) {
        this.status = status;
        return this;
    }

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public CarePlanEntity getCarePlan() {
        return carePlan;
    }

    public void setCarePlan(CarePlanEntity carePlan) {
        this.carePlan = carePlan;
    }

    public PatientEntity getAuthorPatient() {
        return authorPatient;
    }

    public void setAuthorPatient(PatientEntity authorPatient) {
        this.authorPatient = authorPatient;
    }

    public PractitionerEntity getAuthorPractitioner() {
        return authorPractitioner;
    }

    public void setAuthorPractitioner(PractitionerEntity authorPractitioner) {
        this.authorPractitioner = authorPractitioner;
    }

    public QuestionnaireEntity getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(QuestionnaireEntity questionnaire) {
        this.questionnaire = questionnaire;
    }

    public Set<QuestionnaireResponseItem> getItems() {
        return items;
    }

    public void setItems(Set<QuestionnaireResponseItem> items) {
        this.items = items;
    }
}
