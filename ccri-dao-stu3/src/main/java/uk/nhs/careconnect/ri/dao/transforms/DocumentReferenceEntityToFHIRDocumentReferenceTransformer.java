package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceAttachment;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceAuthor;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceIdentifier;

@Component
public class DocumentReferenceEntityToFHIRDocumentReferenceTransformer implements Transformer<DocumentReferenceEntity, DocumentReference> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DocumentReferenceEntityToFHIRDocumentReferenceTransformer.class);

    @Override
    public DocumentReference transform(final DocumentReferenceEntity documentReferenceEntity) {
        final DocumentReference documentReference = new DocumentReference();

        Meta meta = new Meta();
                //.addProfile(CareConnectProfile.Condition_1);

        if (documentReferenceEntity.getUpdated() != null) {
            meta.setLastUpdated(documentReferenceEntity.getUpdated());
        }
        else {
            if (documentReferenceEntity.getCreated() != null) {
                meta.setLastUpdated(documentReferenceEntity.getCreated());
            }
        }
        documentReference.setMeta(meta);

        documentReference.setId(documentReferenceEntity.getId().toString());

        for (DocumentReferenceIdentifier identifier : documentReferenceEntity.getIdentifiers()) {
            documentReference.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (documentReferenceEntity.getCreated() != null) {
            documentReference.setCreated(documentReferenceEntity.getCreated());
        }
        if (documentReferenceEntity.getPatient() != null) {
            documentReference
                    .setSubject(new Reference("Patient/"+documentReferenceEntity.getPatient().getId())
                    .setDisplay(documentReferenceEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (documentReferenceEntity.getType() != null) {
            documentReference.getType().addCoding()
                    .setCode(documentReferenceEntity.getType().getCode())
                    .setDisplay(documentReferenceEntity.getType().getDisplay())
                    .setSystem(documentReferenceEntity.getType().getSystem());
        }
        if (documentReferenceEntity.getCreated() != null) {
            documentReference.setCreated(documentReferenceEntity.getCreated());
        }
        if (documentReferenceEntity.getIndexed() != null) {
            documentReference.setIndexed(documentReferenceEntity.getIndexed());
        }

        if (documentReferenceEntity.getStatus() != null) {
            documentReference.setStatus(documentReferenceEntity.getStatus());
        }

        for (DocumentReferenceAuthor author : documentReferenceEntity.getAuthors()) {
            switch(author.getAuthorType()) {
                case Patient:
                    documentReference.addAuthor()
                            .setReference("Patient/"+author.getPatient().getId())
                            .setDisplay(author.getPatient().getNames().get(0).getDisplayName());
                    break;
                case Practitioner:
                    documentReference.addAuthor()
                            .setReference("Practitioner/"+author.getPractitioner().getId())
                            .setDisplay(author.getPractitioner().getNames().get(0).getDisplayName());
                    break;
                case Organisation:
                    documentReference.addAuthor()
                            .setReference("Organization/"+author.getOrganisation().getId())
                            .setDisplay(author.getOrganisation().getName());
                    break;
            }
        }
        if (documentReferenceEntity.getCustodian() != null) {
            documentReference.setCustodian(new Reference("Organization/"+documentReferenceEntity.getCustodian().getId())
                    .setDisplay(documentReferenceEntity.getCustodian().getName()) );
        }

        for (DocumentReferenceAttachment attachment : documentReferenceEntity.getAttachments()) {
            DocumentReference.DocumentReferenceContentComponent content = documentReference.addContent();
            if (attachment.getUrl() !=null) content.getAttachment().setUrl(attachment.getUrl());
            if (attachment.getTitle() !=null) content.getAttachment().setTitle(attachment.getTitle());
            if (attachment.getCreation() !=null) content.getAttachment().setCreation(attachment.getCreation());
            if (attachment.getContentType() !=null) content.getAttachment().setContentType(attachment.getContentType());
        }

        // KGM 10/4/2018 add practice setting and type
        if (documentReferenceEntity.getContextPracticeSetting() != null) {
            documentReference.getContext().getPracticeSetting().addCoding()
                    .setCode(documentReferenceEntity.getContextPracticeSetting().getCode())
                    .setDisplay(documentReferenceEntity.getContextPracticeSetting().getDisplay())
                    .setSystem(documentReferenceEntity.getContextPracticeSetting().getSystem());
        }
        // KGM 13/6/2018 Add Facility Type
        if (documentReferenceEntity.getContextFaciltityType() != null) {
            documentReference.getContext().getFacilityType().addCoding()
                    .setCode(documentReferenceEntity.getContextFaciltityType().getCode())
                    .setDisplay(documentReferenceEntity.getContextFaciltityType().getDisplay())
                    .setSystem(documentReferenceEntity.getContextFaciltityType().getSystem());
        }

        if (documentReferenceEntity.getContextEncounter() != null) {
            documentReference.getContext().setEncounter(new Reference("Encounter/"+documentReferenceEntity.getContextEncounter().getId()));
        }

        return documentReference;

    }
}
