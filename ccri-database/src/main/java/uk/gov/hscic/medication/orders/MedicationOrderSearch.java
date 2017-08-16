package uk.gov.hscic.medication.orders;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.medication.MedicationOrderDetails;

@Service
public class MedicationOrderSearch {
    private final MedicationOrderEntityToMedicationOrderDetailsTransformer transformer = new MedicationOrderEntityToMedicationOrderDetailsTransformer();

    @Autowired
    private MedicationOrderRepository medicationOrderRepository;

    public MedicationOrderDetails findMedicationOrderByID(Long id) {
        final MedicationOrderEntity item = medicationOrderRepository.findOne(id);

        return item == null
                ? null
                : transformer.transform(item);
    }

    public List<MedicationOrderDetails> findMedicationOrdersForPatient(Long patientId){
        return medicationOrderRepository.findByPatientId(patientId)
                .stream()
                .map(transformer::transform)
                .collect(Collectors.toList());
    }
}
