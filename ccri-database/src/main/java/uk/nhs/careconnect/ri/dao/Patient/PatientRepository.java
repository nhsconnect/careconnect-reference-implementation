
package uk.nhs.careconnect.ri.dao.Patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

public interface PatientRepository extends JpaRepository<PatientEntity, Long>, QueryDslPredicateExecutor<PatientEntity> {
    PatientEntity findByNhsNumber(String nhsNumber);
   // Long countByDepartmentDepartmentIgnoreCase(String department);
   // List<PatientEntity> findPatientsByDepartmentDepartmentIgnoreCase(String department, Pageable pageable);
    PatientEntity findById(Long id);
}