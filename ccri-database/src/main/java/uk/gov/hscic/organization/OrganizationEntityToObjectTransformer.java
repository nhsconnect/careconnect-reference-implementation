package uk.gov.hscic.organization;

import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.organization.OrganizationDetails;

public class OrganizationEntityToObjectTransformer implements Transformer<OrganizationEntity, OrganizationDetails> {

    @Override
    public OrganizationDetails transform(final OrganizationEntity organizationEntity) {
        OrganizationDetails organization = new OrganizationDetails();

        organization.setId(organizationEntity.getId());
        organization.setOrgCode(organizationEntity.getOrgCode());
        organization.setSiteCode(organizationEntity.getSiteCode());
        organization.setOrgName(organizationEntity.getOrgName());
        organization.setLastUpdated(organizationEntity.getLastUpdated());

        return organization;
    }
}
