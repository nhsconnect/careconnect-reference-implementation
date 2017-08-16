package uk.gov.hscic.patient.immunisations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImmunisationRepository extends JpaRepository<ImmunisationEntity, Long> {
    List<ImmunisationEntity> findByNhsNumber(String nhsNumber);
}
