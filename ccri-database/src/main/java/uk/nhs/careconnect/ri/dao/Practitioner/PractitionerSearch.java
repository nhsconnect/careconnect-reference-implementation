package uk.nhs.careconnect.ri.dao.Practitioner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.model.practitioner.PractitionerDetails;

import java.util.List;
import java.util.stream.Collectors;

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
