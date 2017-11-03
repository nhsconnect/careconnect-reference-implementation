package uk.nhs.careconnect.ri.entity.documentReference;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Enumerations;
import uk.nhs.careconnect.ri.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

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

    @ManyToOne
    @JoinColumn (name = "DOCUMENT_REFERENCE_ID",foreignKey= @ForeignKey(name="FK_DOCUMENT_REFERENCE_IDENTIFIER_DOCUMENT_REFERENCE_ID"))
	@LazyCollection(LazyCollectionOption.TRUE)
    private DocumentReferenceEntity documentReference;



	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }




}
