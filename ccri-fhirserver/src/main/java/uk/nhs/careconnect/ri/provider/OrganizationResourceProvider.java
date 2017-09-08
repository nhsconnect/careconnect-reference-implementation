package uk.nhs.careconnect.ri.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.SystemCode;
import uk.nhs.careconnect.ri.dao.Organisation.OrganisationDao;

import java.util.List;

@Component
public class OrganizationResourceProvider implements IResourceProvider {

    @Autowired
    private OrganisationDao organisationDao;

    @Override
    public Class<Organization> getResourceType() {
        return Organization.class;
    }


    @Read()
    public Organization getOrganizationById(@IdParam IdType organisationId) {
        Organization organisation = organisationDao.read(organisationId);

        if ( organisation == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No patient details found for patient ID: " + organisationId.getIdPart()),
                    SystemCode.PRACTITIONER_NOT_FOUND, OperationOutcome.IssueType.NOTFOUND);
        }
        return organisation;
    }

    @Search
    public List<Organization> searchOrganisation(@RequiredParam(name = Organization.SP_IDENTIFIER) TokenParam identifier) {
       return organisationDao.searchOrganization(identifier);
    }


    

}
