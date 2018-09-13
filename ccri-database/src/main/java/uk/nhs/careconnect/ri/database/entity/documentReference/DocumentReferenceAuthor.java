package uk.nhs.careconnect.ri.database.entity.documentReference;

import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;

@Entity
@Table(name="DocumentReferenceAuthor", uniqueConstraints= @UniqueConstraint(name="PK_DOCUMENT_REFERENCE_AUTHOR", columnNames={"DOCUMENT_REFERENCE_AUTHOR_ID"})
        ,indexes = {}
)
public class DocumentReferenceAuthor {
    public enum author {
        Patient, Practitioner, Organisation, Device
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "DOCUMENT_REFERENCE_AUTHOR_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "DOCUMENT_REFERENCE_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_AUTHOR_DOCUMENT_REFERENCE_ID"))
    private DocumentReferenceEntity documentReference;

    @Enumerated(EnumType.ORDINAL)
    private author authorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="authorPractitioner",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_AUTHOR_PRACTITIONER_ID"))

    private PractitionerEntity authorPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="authorPatient",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_AUTHOR_PATIENT_ID"))

    private PatientEntity authorPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="authorOrganisation",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_AUTHOR_ORGANISATION_ID"))

    private OrganisationEntity authorOrganisation;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public DocumentReferenceEntity getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReferenceEntity documentReference) {
        this.documentReference = documentReference;
    }

    public OrganisationEntity getOrganisation() {
        return authorOrganisation;
    }

    public PatientEntity getPatient() {
        return authorPatient;
    }

    public PractitionerEntity getPractitioner() {
        return authorPractitioner;
    }

    public DocumentReferenceAuthor setOrganisation(OrganisationEntity authorOrganisation) {
        this.authorOrganisation = authorOrganisation;
        return this;
    }

    public DocumentReferenceAuthor setPatient(PatientEntity performerPatient) {
        this.authorPatient = performerPatient;
        return this;
    }

    public DocumentReferenceAuthor setPractitioner(PractitionerEntity performerPractitioner) {
        this.authorPractitioner = performerPractitioner;
        return this;
    }

    public author getAuthorType() {
        return authorType;
    }

    public void setAuthorType(author performerType) {
        this.authorType = performerType;
    }
}
