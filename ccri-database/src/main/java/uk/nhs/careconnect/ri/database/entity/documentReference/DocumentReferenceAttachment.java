package uk.nhs.careconnect.ri.database.entity.documentReference;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="DocumentReferenceAttachment", uniqueConstraints= @UniqueConstraint(name="PK_DOCUMENT_REFERENCE_ATTACHMENT", columnNames={"DOCUMENT_REFERENCE_ATTACHMENT_ID"})
        ,indexes = {}
)
public class DocumentReferenceAttachment {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "DOCUMENT_REFERENCE_ATTACHMENT_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "DOCUMENT_REFERENCE_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_ATTACHMENT_DOCUMENT_REFERENCE_ID"))
    private DocumentReferenceEntity documentReference;

    @Column(name="url")
    private String url;

    @Column(name="contentType")
    private String contentType;

    @Column(name="title")
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation")
    private Date creation;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public DocumentReferenceEntity getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReferenceEntity documentReference) {
        this.documentReference = documentReference;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }
}
