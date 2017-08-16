package uk.gov.hscic.medication.administration;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.medication.MedicationAdministrationDetail;

@Service
public class MedicationAdministrationSearch {
    private final MedicationAdministrationEntityToMedicationAdministrationDetailTransformer transformer = new MedicationAdministrationEntityToMedicationAdministrationDetailTransformer();

    @Autowired
    private MedicationAdministrationRepository medicationAdministrationRepository;

    public MedicationAdministrationDetail findMedicationAdministrationByID(Long id) {
        MedicationAdministrationEntity item = medicationAdministrationRepository.findOne(id);

        return item == null
                ? null
                : transformer.transform(item);
    }

    public List<MedicationAdministrationDetail> findMedicationAdministrationForPatient(Long patientId) {
        return medicationAdministrationRepository.findByPatientId(patientId)
                .stream()
                .map(transformer::transform)
                .collect(Collectors.toList());
    }
}
