package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

import java.util.List;


public interface OrganisationRepository extends BaseInterface {

   void save(FhirContext ctx, OrganisationEntity organization);
   Organization read(FhirContext ctx,IdType theId);

    Organization create(FhirContext ctx,Organization organization, @IdParam IdType theId, @ConditionalUrlParam String theConditional);

    OrganisationEntity readEntity(FhirContext ctx,IdType theId);


   List<Organization> searchOrganization (FhirContext ctx,
            @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Organization.SP_NAME) StringParam name,
            @OptionalParam(name = Organization.SP_ADDRESS_POSTALCODE) StringParam postCode
    );


}
