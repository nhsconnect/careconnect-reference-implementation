package uk.nhs.careconnect.ri.database.entity.questionnaire;


import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.ResourceType;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Questionnaire",
        indexes = {

        })
public class QuestionnaireEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum QuestionnaireType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="QUESTIONNAIRE_ID")
    private Long id;

    @OneToMany(mappedBy="questionnaire", targetEntity=QuestionnaireIdentifier.class)
    private Set<QuestionnaireIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Enumerations.PublicationStatus status;

    @Column(name="VERSION",length = MAX_DESC_LENGTH,nullable = true)
    private String version;

    @Column(name="NAME",length = MAX_DESC_LENGTH,nullable = true)
    private String name;

    @Column(name="TITLE",length = MAX_DESC_LENGTH,nullable = true)
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATETIME")
    private Date dateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "APPROVAL_DATETIME")
    private Date approvalDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_REVIEW_DATETIME")
    private Date lastReviewDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_QUESTIONNAIRE_CODE_CONCEPT_ID"))
    private ConceptEntity questionnaireCode;

    @Column(name = "SUBJECT_TYPE")
    private ResourceType subjectType;

    @OneToMany(mappedBy="questionnaire", targetEntity=QuestionnaireItem.class)
    private Set<QuestionnaireItem> items = new HashSet<>();

    public Set<QuestionnaireItem> getItems() {
        return items;
    }

    public void setItems(Set<QuestionnaireItem> items) {
        this.items = items;
    }

    public Enumerations.PublicationStatus getStatus() {
        return status;
    }

    public void setStatus(Enumerations.PublicationStatus status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Date getApprovalDateTime() {
        return approvalDateTime;
    }

    public void setApprovalDateTime(Date approvalDateTime) {
        this.approvalDateTime = approvalDateTime;
    }

    public Date getLastReviewDateTime() {
        return lastReviewDateTime;
    }

    public void setLastReviewDateTime(Date lastReviewDateTime) {
        this.lastReviewDateTime = lastReviewDateTime;
    }

    public ConceptEntity getQuestionnaireCode() {
        return questionnaireCode;
    }

    public void setQuestionnaireCode(ConceptEntity questionnaireCode) {
        this.questionnaireCode = questionnaireCode;
    }

    public ResourceType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(ResourceType subjectType) {
        this.subjectType = subjectType;
    }

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }



    public Set<QuestionnaireIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<QuestionnaireIdentifier>(); }
        return identifiers;
    }



    public QuestionnaireEntity setIdentifiers(Set<QuestionnaireIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }


}
