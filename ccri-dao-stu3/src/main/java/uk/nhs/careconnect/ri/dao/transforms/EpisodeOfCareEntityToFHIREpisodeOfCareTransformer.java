package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareIdentifier;

@Component
public class EpisodeOfCareEntityToFHIREpisodeOfCareTransformer implements Transformer<EpisodeOfCareEntity, EpisodeOfCare> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EpisodeOfCareEntityToFHIREpisodeOfCareTransformer.class);

    @Override
    public EpisodeOfCare transform(final EpisodeOfCareEntity episodeEntity) {
        final EpisodeOfCare episode = new EpisodeOfCare();

        Meta meta = new Meta();
        //.addProfile(CareConnectProfile.EpisodeOfCare_1);

        if (episodeEntity.getUpdated() != null) {
            meta.setLastUpdated(episodeEntity.getUpdated());
        }
        else {
            if (episodeEntity.getCreated() != null) {
                meta.setLastUpdated(episodeEntity.getCreated());
            }
        }
        episode.setMeta(meta);

        episode.setId(episodeEntity.getId().toString());

        for(EpisodeOfCareIdentifier identifier : episodeEntity.getIdentifiers())
        {
            episode.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }
        if (episodeEntity.getCareManager() != null) {
            episode.setCareManager(new Reference("Practitioner/"+episodeEntity.getCareManager().getId()));
            episode.getCareManager().setDisplay(episodeEntity.getCareManager().getNames().get(0).getDisplayName());
        }
        if (episodeEntity.getPatient() != null) {
            episode
                    .setPatient(new Reference("Patient/"+episodeEntity.getPatient().getId())
                    .setDisplay(episodeEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (episodeEntity.getManagingOrganisation() != null) {
            episode
                    .setManagingOrganization(new Reference("Organization/"+episodeEntity.getManagingOrganisation().getId())
                    .setDisplay(episodeEntity.getManagingOrganisation().getName()));
        }
        if (episodeEntity.getType() != null) {
            episode.addType().addCoding()
                    .setCode(episodeEntity.getType().getCode())
                    .setSystem(episodeEntity.getType().getSystem())
                    .setDisplay(episodeEntity.getType().getDisplay());
        }
        if (episodeEntity.getStatus() != null) {
            episode.setStatus(episodeEntity.getStatus());
        }

        if (episodeEntity.getPeriodStartDate() != null || episodeEntity.getPeriodEndDate() != null)
        {
            Period period = new Period();
            if (episodeEntity.getPeriodStartDate() != null ) {
               period.setStart(episodeEntity.getPeriodStartDate());
            }
            if (episodeEntity.getPeriodEndDate() != null) {
                period.setEnd(episodeEntity.getPeriodEndDate());
            }
            episode.setPeriod(period);
        }


        return episode;

    }
}
