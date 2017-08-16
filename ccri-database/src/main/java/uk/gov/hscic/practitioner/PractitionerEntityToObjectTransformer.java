package uk.gov.hscic.practitioner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.practitioner.PractitionerDetails;

public class PractitionerEntityToObjectTransformer implements Transformer<PractitionerEntity, PractitionerDetails> {

    @Override
    public PractitionerDetails transform(final PractitionerEntity practitionerEntity) {
        PractitionerDetails practitioner = new PractitionerDetails();

        List<String> roleIds = Arrays.asList(practitionerEntity.getRoleIds().split("\\|"))
                .stream()
                .filter(roleId -> !roleId.isEmpty())
                .collect(Collectors.toList());

        practitioner.setId(practitionerEntity.getId());
        practitioner.setUserId(practitionerEntity.getUserId());
        practitioner.setRoleIds(roleIds);
        practitioner.setNameFamily(practitionerEntity.getNameFamily());
        practitioner.setNameGiven(practitionerEntity.getNameGiven());
        practitioner.setNamePrefix(practitionerEntity.getNamePrefix());
        practitioner.setGender(practitionerEntity.getGender());
        practitioner.setOrganizationId(practitionerEntity.getOrganizationId());
        practitioner.setRoleCode(practitionerEntity.getRoleCode());
        practitioner.setRoleDisplay(practitionerEntity.getRoleDisplay());
        practitioner.setComCode(practitionerEntity.getComCode());
        practitioner.setComDisplay(practitionerEntity.getComDisplay());
        practitioner.setLastUpdated(practitionerEntity.getLastUpdated());

        return practitioner;
    }
}
