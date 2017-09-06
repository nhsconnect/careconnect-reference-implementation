
package uk.nhs.careconnect.ri.entity.patient;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface PatientRepository extends JpaRepository<PatientEntity, Long>, QueryDslPredicateExecutor<PatientEntity> {
    PatientEntity findByNhsNumber(String nhsNumber);
   // Long countByDepartmentDepartmentIgnoreCase(String department);
   // List<PatientEntity> findPatientsByDepartmentDepartmentIgnoreCase(String department, Pageable pageable);
    PatientEntity findById(Long id);
}