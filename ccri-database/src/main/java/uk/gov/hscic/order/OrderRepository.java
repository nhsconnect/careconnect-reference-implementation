package uk.gov.hscic.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findBySubjectPatientId(Long patientId);
}
