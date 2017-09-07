package uk.nhs.careconnect.ri.dao.Organisation;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<OrganisationEntity, Long> {
    List<OrganisationEntity> findByOrgCode(String orgCode);
    List<OrganisationEntity> findBySiteCode(String siteCode);
}
