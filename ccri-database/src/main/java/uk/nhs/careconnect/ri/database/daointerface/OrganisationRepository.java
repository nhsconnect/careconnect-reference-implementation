package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;

import java.util.List;


public interface OrganisationRepository extends BaseRepository<OrganisationEntity,Organization> {

   void save(FhirContext ctx, OrganisationEntity organization) throws OperationOutcomeException;
   Organization read(FhirContext ctx,IdType theId);

    Organization create(FhirContext ctx,Organization organization, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    OrganisationEntity readEntity(FhirContext ctx,IdType theId);


   List<Organization> searchOrganization (FhirContext ctx,
            @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Organization.SP_NAME) StringParam name,
            @OptionalParam(name = Organization.SP_ADDRESS_POSTALCODE) StringParam postCode
           ,@OptionalParam(name= Organization.SP_RES_ID) StringParam id
    );


}
