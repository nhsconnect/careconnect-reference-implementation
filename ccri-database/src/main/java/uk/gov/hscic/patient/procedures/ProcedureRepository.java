package uk.gov.hscic.patient.procedures;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureRepository extends JpaRepository<ProcedureEntity, Long> { }
