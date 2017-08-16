package uk.gov.hscic.practitioner;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.practitioner.PractitionerDetails;

@Service
public class PractitionerSearch {
    private final PractitionerEntityToObjectTransformer transformer = new PractitionerEntityToObjectTransformer();

    @Autowired
    private PractitionerRepository practitionerRepository;

    public PractitionerDetails findPractitionerDetails(final String practitionerId) {
        final PractitionerEntity item = practitionerRepository.findOne(Long.parseLong(practitionerId));

        return item == null
                ? null
                : transformer.transform(item);
    }

    public List<PractitionerDetails> findPractitionerByUserId(final String practitionerUserId) {
        return practitionerRepository.findByUserId(practitionerUserId)
                .stream()
                .map(transformer::transform)
                .collect(Collectors.toList());
    }
}
