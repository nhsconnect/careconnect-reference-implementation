package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import mayfieldis.careconnect.nosql.entities.DocumentReferenceEntity;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;


import javax.transaction.Transactional;

@Transactional
@Repository
public class DocumentReferenceDao implements IDocumentReference {


    @Autowired
    MongoOperations mongo;

    private static final Logger log = LoggerFactory.getLogger(DocumentReferenceDao.class);

    public DocumentReference create(FhirContext ctx, DocumentReference documentReference, IdType theId, String theConditional) {
        log.debug("DocumentReferenceEntity.save");




        DocumentReferenceEntity documentReferenceEntity = new DocumentReferenceEntity();
        documentReferenceEntity.setName("Dina");
        String json = ctx.newJsonParser().encodeResourceToString(documentReference);
        documentReferenceEntity.setJson(json);

        mongo.save(documentReferenceEntity);

        return null;
    }
}
