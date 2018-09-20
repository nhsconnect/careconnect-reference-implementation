package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.OrganisationRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class OrganizationProvider implements ICCResourceProvider {

    @Autowired
    private OrganisationRepository organisationDao;

    @Override
    public Class<Organization> getResourceType() {
        return Organization.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return organisationDao.count();
    }

    @Update
    public MethodOutcome updateOrganization(HttpServletRequest theRequest,@ResourceParam Organization organization, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
                Organization newOrganization = organisationDao.create(ctx, organization, theId, theConditional);
                method.setId(newOrganization.getIdElement());
                method.setResource(newOrganization);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Create
    public MethodOutcome createOrganization(HttpServletRequest theRequest,@ResourceParam Organization organization) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
            Organization newOrganization = organisationDao.create(ctx, organization,null,null);
            method.setId(newOrganization.getIdElement());
            method.setResource(newOrganization);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Read()
    public Organization getOrganizationById(@IdParam IdType organisationId) {
        Organization organisation = organisationDao.read(ctx, organisationId);

        if ( organisation == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Organization/" + organisationId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }
        return organisation;
    }

    @Search
    public List<Organization> searchOrganisation(HttpServletRequest theRequest,
                                                @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier,
                                                 @OptionalParam(name = Organization.SP_NAME) StringParam name,
                                                 @OptionalParam(name = Organization.SP_ADDRESS_POSTALCODE) StringParam postCode
            , @OptionalParam(name = Organization.SP_RES_ID) StringParam resid
    ) {
       return organisationDao.searchOrganization(ctx, identifier,name, postCode,resid);
    }


    

}
