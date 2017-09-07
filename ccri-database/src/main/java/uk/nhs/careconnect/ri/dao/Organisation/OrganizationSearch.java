package uk.nhs.careconnect.ri.dao.Organisation;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.model.organization.OrganizationDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationSearch {
    private final OrganizationEntityToObjectTransformer transformer = new OrganizationEntityToObjectTransformer();

    @Autowired
    private OrganizationRepository organizationRepository;

    public OrganizationDetails findOrganizationDetails(final String organizationId) {
        final OrganisationEntity item = organizationRepository.findOne(Long.parseLong(organizationId));

        return item == null
                ? null
                : transformer.transform(item);
    }

    public List<OrganizationDetails> findOrganizationDetailsByOrgODSCode(String organizationODSCode) {
        return organizationRepository.findByOrgCode(organizationODSCode)
                .stream()
                .map(transformer::transform)
                .collect(Collectors.toList());
    }

    public List<OrganizationDetails> findOrganizationDetailsBySiteODSCode(String siteODSCode) {
        List<OrganisationEntity> organizationEntities = organizationRepository.findBySiteCode(siteODSCode);

        // First, find a list of entries that only have a site code
        List<OrganizationDetails> orgsWithNoOrgCode = organizationEntities
                .stream()
                .filter(entity -> StringUtils.isBlank(entity.getOrgCode()))
                .map(transformer::transform)
                .collect(Collectors.toList());

        // Now use the list of known org codes, to get the full list of site codes for each org
        List<OrganizationDetails> orgsFromOrgCode = organizationEntities
                .stream()
                .map(OrganisationEntity::getOrgCode)
                .filter(orgCode -> StringUtils.isNotBlank(orgCode))
                .distinct()
                .map(this::findOrganizationDetailsByOrgODSCode)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return ListUtils.union(orgsWithNoOrgCode, orgsFromOrgCode);
    }
}
