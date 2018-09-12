package uk.nhs.careconnect.ri.database.entity.consent;

import org.hl7.fhir.dstu3.model.Consent;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Consent",
        indexes = {

        })
public class ConsentEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum ConsentType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CONSENT_ID")
    private Long id;

    @OneToMany(mappedBy="consent", targetEntity=ConsentIdentifier.class)
    private Set<ConsentIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Consent.ConsentState status;

    @OneToMany(mappedBy="consent", targetEntity=ConsentCategory.class)
    private Set<ConsentCategory> categories = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_PATIENT_ID"))
    private PatientEntity patient;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_START_DATETIME")
    private Date periodStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_END_DATETIME")
    private Date periodEndDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATETIME")
    private Date DateTime;

    @OneToMany(mappedBy="consent", targetEntity=ConsentParty.class)
    private Set<ConsentParty> parties = new HashSet<>();

    @OneToMany(mappedBy="consent", targetEntity=ConsentActor.class)
    private Set<ConsentActor> actors = new HashSet<>();

    @OneToMany(mappedBy="consent", targetEntity= ConsentAction.class)
    private Set<ConsentAction> actions = new HashSet<>();

    @OneToMany(mappedBy="consent", targetEntity= ConsentOrganisation.class)
    private Set<ConsentOrganisation> organisations = new HashSet<>();

    @Column(name="SOURCE_SYSTEM")
    private String sourceSystem;

    @Column(name="SOURCE_VALUE")
    private String sourceValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SOURCE_FORM_ID",nullable = true,foreignKey= @ForeignKey(name="FK_CONSENT_SOURCE_FORM_ID"))
    private QuestionnaireResponseEntity form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SOURCE_DOCUMENT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_CONSENT_SOURCE_DOCUMENT_ID"))
    private DocumentReferenceEntity document;

    @Column(name="POLICY_RULE")
    private String policyRule;

    @OneToMany(mappedBy="consent", targetEntity= ConsentPolicy.class)
    private Set<ConsentPolicy> policies = new HashSet<>();

    @OneToMany(mappedBy="consent", targetEntity= ConsentPurpose.class)
    private Set<ConsentPurpose> purposes = new HashSet<>();


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



    public Set<ConsentIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<ConsentIdentifier>(); }
        return identifiers;
    }


    public ConsentEntity setIdentifiers(Set<ConsentIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }



    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }


    public Date getPeriodStartDateTime() {
        return periodStartDateTime;
    }

    public void setPeriodStartDateTime(Date periodStartDateTime) {
        this.periodStartDateTime = periodStartDateTime;
    }

    public Date getPeriodEndDateTime() {
        return periodEndDateTime;
    }

    public void setPeriodEndDateTime(Date periodEndDateTime) {
        this.periodEndDateTime = periodEndDateTime;
    }

    public Date getDateTime() {
        return DateTime;
    }

    public void setDateTime(Date dateTime) {
        DateTime = dateTime;
    }

    public Consent.ConsentState getStatus() {
        return status;
    }

    public void setStatus(Consent.ConsentState status) {
        this.status = status;
    }

    public Set<ConsentCategory> getCategories() {
        return categories;
    }

    public void setCategories(Set<ConsentCategory> categories) {
        this.categories = categories;
    }

    public Set<ConsentParty> getParties() {
        return parties;
    }

    public void setParties(Set<ConsentParty> parties) {
        this.parties = parties;
    }

    public Set<ConsentActor> getActors() {
        return actors;
    }

    public void setActors(Set<ConsentActor> actors) {
        this.actors = actors;
    }

    public Set<ConsentAction> getActions() {
        return actions;
    }

    public void setActions(Set<ConsentAction> actions) {
        this.actions = actions;
    }

    public Set<ConsentOrganisation> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Set<ConsentOrganisation> organisations) {
        this.organisations = organisations;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public QuestionnaireResponseEntity getForm() {
        return form;
    }

    public void setForm(QuestionnaireResponseEntity form) {
        this.form = form;
    }

    public DocumentReferenceEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentReferenceEntity document) {
        this.document = document;
    }

    public String getPolicyRule() {
        return policyRule;
    }

    public void setPolicyRule(String policyRule) {
        this.policyRule = policyRule;
    }

    public Set<ConsentPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(Set<ConsentPolicy> policies) {
        this.policies = policies;
    }

    public Set<ConsentPurpose> getPurposes() {
        return purposes;
    }

    public void setPurposes(Set<ConsentPurpose> purposes) {
        this.purposes = purposes;
    }
}
