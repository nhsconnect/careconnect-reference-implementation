package uk.nhs.careconnect.ri.dao.Practitioner;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import java.util.List;

public interface PractitionerRepository extends JpaRepository<PractitionerEntity, Long> {
    List<PractitionerEntity> findByUserId(String practitionerUserId);
}
