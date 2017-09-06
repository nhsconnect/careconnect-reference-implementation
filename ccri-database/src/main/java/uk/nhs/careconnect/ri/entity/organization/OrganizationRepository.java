package uk.nhs.careconnect.ri.entity.organization;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<OrganisationEntity, Long> {
    List<OrganisationEntity> findByOrgCode(String orgCode);
    List<OrganisationEntity> findBySiteCode(String siteCode);
}
