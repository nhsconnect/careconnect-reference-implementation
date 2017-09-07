package uk.nhs.careconnect.ri.dao.Location;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;

import java.util.List;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    List<LocationEntity> findBySiteOdsCode(String siteOdsCode);
    List<LocationEntity> findByOrgOdsCode(String orgOdsCode);
}
