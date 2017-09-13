package uk.nhs.careconnect.ri.dao.Organisation;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Organization;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

import java.util.List;


public interface OrganisationRepository {

   void save(OrganisationEntity organization);
   Organization read(IdType theId);

   List<Organization> searchOrganization (
            @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    );


}
