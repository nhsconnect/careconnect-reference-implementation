package uk.nhs.careconnect.ri.dao.Organisation;

import org.apache.commons.collections4.Transformer;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.model.organization.OrganizationDetails;

public class OrganizationEntityToObjectTransformer implements Transformer<OrganisationEntity, OrganizationDetails> {

    @Override
    public OrganizationDetails transform(final OrganisationEntity organizationEntity) {
        OrganizationDetails organization = new OrganizationDetails();

        organization.setId(organizationEntity.getId());
        organization.setOrgCode(organizationEntity.getOrgCode());
        organization.setSiteCode(organizationEntity.getSiteCode());
        organization.setOrgName(organizationEntity.getOrgName());
        organization.setLastUpdated(organizationEntity.getLastUpdated());

        return organization;
    }
}
