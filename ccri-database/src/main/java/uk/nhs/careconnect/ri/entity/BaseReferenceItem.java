package uk.nhs.careconnect.ri.entity;

import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.entity.goal.GoalEntity;
import uk.nhs.careconnect.ri.entity.list.ListEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseEntity;

import javax.persistence.*;

@MappedSuperclass
public class BaseReferenceItem extends BaseResource {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VALUE_GOAL_ID")
    GoalEntity goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "VALUE_CONDITION_ID")
    ConditionEntity condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "VALUE_OBSERVATION_ID")
    ObservationEntity observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "VALUE_PATIENT_ID")
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "VALUE_DOCUMENT_REFERENCE_ID")
    private DocumentReferenceEntity documentReference;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "VALUE_LIST_ID")
    private ListEntity
            listResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "VALUE_FORM_ID")
    private QuestionnaireResponseEntity
            form;

    @Override
    public Long getId() {
        return null;
    }

    public GoalEntity getGoal() {
        return goal;
    }

    public void setGoal(GoalEntity goal) {
        this.goal = goal;
    }

    public ConditionEntity getCondition() {
        return condition;
    }

    public void setCondition(ConditionEntity condition) {
        this.condition = condition;
    }

    public ObservationEntity getObservation() {
        return observation;
    }

    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public DocumentReferenceEntity getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReferenceEntity documentReference) {
        this.documentReference = documentReference;
    }

    public ListEntity getListResource() {
        return listResource;
    }

    public void setListResource(ListEntity listResource) {
        this.listResource = listResource;
    }

    public QuestionnaireResponseEntity getForm() {
        return form;
    }

    public void setForm(QuestionnaireResponseEntity form) {
        this.form = form;
    }
}
