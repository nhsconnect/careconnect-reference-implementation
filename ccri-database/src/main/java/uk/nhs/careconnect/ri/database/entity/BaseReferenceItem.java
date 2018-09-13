package uk.nhs.careconnect.ri.database.entity;

import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.consent.ConsentEntity;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.goal.GoalEntity;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.MedicationStatementEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentEntity;
import uk.nhs.careconnect.ri.database.entity.list.ListEntity;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.RelatedPersonEntity;

import javax.persistence.*;

@MappedSuperclass
public class BaseReferenceItem extends BaseResource {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_CONDITION_ID")
    ConditionEntity referenceCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_OBSERVATION_ID")
    ObservationEntity referenceObservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_PATIENT_ID")
    private PatientEntity referencePatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_DOCUMENT_REFERENCE_ID")
    private DocumentReferenceEntity referenceDocumentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_CARE_TEAM_ID")
    private CareTeamEntity ReferenceCareTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_LIST_ID")
    private ListEntity
            referenceListResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_FORM_ID")
    private QuestionnaireResponseEntity
            referenceForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "REF_GOAL_ID")
    private GoalEntity referenceGoal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_STATEMENT_ID")
    private MedicationStatementEntity referenceStatement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_PROCEDURE_ID")
    private ProcedureEntity referenceProcedure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_RISK_ID")
    private RiskAssessmentEntity referenceRisk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_ORGANISATION_ID")
    private OrganisationEntity referenceOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_PRACTITIONER_ID")
    private PractitionerEntity referencePractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_PERSON_ID")
    private RelatedPersonEntity referencePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_IMPRESSION_ID")
    private ClinicalImpressionEntity referenceClinicalImpression;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REF_CONSENT_ID")
    private ConsentEntity referenceConsent;

    @Override
    public Long getId() {
        return null;
    }

    public ConditionEntity getReferenceCondition() {
        return referenceCondition;
    }

    public void setReferenceCondition(ConditionEntity referenceCondition) {
        this.referenceCondition = referenceCondition;
    }

    public ObservationEntity getReferenceObservation() {
        return referenceObservation;
    }

    public void setReferenceObservation(ObservationEntity referenceObservation) {
        this.referenceObservation = referenceObservation;
    }

    public PatientEntity getReferencePatient() {
        return referencePatient;
    }

    public void setReferencePatient(PatientEntity referencePatient) {
        this.referencePatient = referencePatient;
    }

    public DocumentReferenceEntity getReferenceDocumentReference() {
        return referenceDocumentReference;
    }

    public void setReferenceDocumentReference(DocumentReferenceEntity referenceDocumentReference) {
        this.referenceDocumentReference = referenceDocumentReference;
    }

    public ListEntity getReferenceListResource() {
        return referenceListResource;
    }

    public void setReferenceListResource(ListEntity referenceListResource) {
        this.referenceListResource = referenceListResource;
    }

    public QuestionnaireResponseEntity getReferenceForm() {
        return referenceForm;
    }

    public void setReferenceForm(QuestionnaireResponseEntity referenceForm) {
        this.referenceForm = referenceForm;
    }

    public GoalEntity getReferenceGoal() {
        return referenceGoal;
    }

    public void setReferenceGoal(GoalEntity referenceGoal) {
        this.referenceGoal = referenceGoal;
    }

    public MedicationStatementEntity getReferenceStatement() {
        return referenceStatement;
    }

    public void setReferenceStatement(MedicationStatementEntity referenceStatement) {
        this.referenceStatement = referenceStatement;
    }

    public ProcedureEntity getReferenceProcedure() {
        return referenceProcedure;
    }

    public void setReferenceProcedure(ProcedureEntity referenceProcedure) {
        this.referenceProcedure = referenceProcedure;
    }

    public RiskAssessmentEntity getReferenceRisk() {
        return referenceRisk;
    }

    public void setReferenceRisk(RiskAssessmentEntity referenceRisk) {
        this.referenceRisk = referenceRisk;
    }

    public CareTeamEntity getReferenceCareTeam() {
        return ReferenceCareTeam;
    }

    public void setReferenceCareTeam(CareTeamEntity referenceCareTeam) {
        ReferenceCareTeam = referenceCareTeam;
    }

    public OrganisationEntity getReferenceOrganisation() {
        return referenceOrganisation;
    }

    public void setReferenceOrganisation(OrganisationEntity referenceOrganisation) {
        this.referenceOrganisation = referenceOrganisation;
    }

    public PractitionerEntity getReferencePractitioner() {
        return referencePractitioner;
    }

    public void setReferencePractitioner(PractitionerEntity referencePractitioner) {
        this.referencePractitioner = referencePractitioner;
    }

    public RelatedPersonEntity getReferencePerson() {
        return referencePerson;
    }

    public void setReferencePerson(RelatedPersonEntity referencePerson) {
        this.referencePerson = referencePerson;
    }

    public ClinicalImpressionEntity getReferenceClinicalImpression() {
        return referenceClinicalImpression;
    }

    public void setReferenceClinicalImpression(ClinicalImpressionEntity referenceClinicalImpression) {
        this.referenceClinicalImpression = referenceClinicalImpression;
    }

    public ConsentEntity getReferenceConsent() {
        return referenceConsent;
    }

    public void setReferenceConsent(ConsentEntity referenceConsent) {
        this.referenceConsent = referenceConsent;
    }
}
