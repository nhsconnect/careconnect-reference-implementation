package uk.nhs.careconnect.ri.entity.medications;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<MedicationEntity, Long> { }