package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;

import java.util.List;

public interface IComposition {

    Composition read(FhirContext ctx, IdType theId);

    Bundle readDocument(FhirContext ctx, IdType theId);

    List<Resource> search(FhirContext ctx, TokenParam resid
            ,ReferenceParam patient);

    Bundle buildSummaryCareDocument(IGenericClient client, IdType patientId);

    Bundle buildEncounterDocument(IGenericClient client, IdType encounterId);
}
