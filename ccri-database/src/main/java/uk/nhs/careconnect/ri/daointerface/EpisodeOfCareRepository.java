package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;

import java.util.List;

public interface EpisodeOfCareRepository {
    void save(EpisodeOfCare episode);

    EpisodeOfCare read(IdType theId);

    EpisodeOfCare create(EpisodeOfCare episode, @IdParam IdType theId, @ConditionalUrlParam String theConditional);


    List<EpisodeOfCare> search(

            @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date

            );

    List<EpisodeOfCareEntity> searchEntity(
            @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date

    );
}
