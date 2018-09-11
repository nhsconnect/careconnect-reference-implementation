package uk.nhs.careconnect.ri.database.entity.documentReference;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="DocumentReferenceIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_DOCUMENT_REFERENCE_IDENTIFIER", columnNames={"DOCUMENT_REFERENCE_IDENTIFIER_ID"})
		,indexes = {}
		)
public class DocumentReferenceIdentifier extends BaseIdentifier {

	public DocumentReferenceIdentifier() {
	}
    public DocumentReferenceIdentifier(DocumentReferenceEntity documentReference) {
		this.documentReference = documentReference;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "DOCUMENT_REFERENCE_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "DOCUMENT_REFERENCE_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_IDENTIFIER_DOCUMENT_REFERENCE_ID"))

    private DocumentReferenceEntity documentReference;


	public DocumentReferenceEntity getDocumentReference() {
		return documentReference;
	}

	public void setDocumentReference(DocumentReferenceEntity documentReference) {
		this.documentReference = documentReference;
	}

	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }




}
