package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;

public interface ISearchResultDao {

    String save(IBundleProvider searchResults);

    IBundleProvider read(String searchId);
}
