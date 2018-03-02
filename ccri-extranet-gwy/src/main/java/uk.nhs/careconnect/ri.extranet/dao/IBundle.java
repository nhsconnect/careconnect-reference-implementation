package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;

public interface IBundle {

    OperationOutcome create(FhirContext ctx, Bundle bundle, IdType theId, String theConditional);
}
