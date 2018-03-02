package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.IdType;

public interface IDocumentReference {

    DocumentReference create(FhirContext ctx, DocumentReference documentReference, IdType theId, String theConditional);
}
