package uk.gov.hscic.organization;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
    List<OrganizationEntity> findByOrgCode(String orgCode);
    List<OrganizationEntity> findBySiteCode(String siteCode);
}
