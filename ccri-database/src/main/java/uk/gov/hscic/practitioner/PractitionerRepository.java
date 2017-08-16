package uk.gov.hscic.practitioner;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PractitionerRepository extends JpaRepository<PractitionerEntity, Long> {
    List<PractitionerEntity> findByUserId(String practitionerUserId);
}
