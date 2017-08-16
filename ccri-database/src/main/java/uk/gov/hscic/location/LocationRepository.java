package uk.gov.hscic.location;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    List<LocationEntity> findBySiteOdsCode(String siteOdsCode);
    List<LocationEntity> findByOrgOdsCode(String orgOdsCode);
}
