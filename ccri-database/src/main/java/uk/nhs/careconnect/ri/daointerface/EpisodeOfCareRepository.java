package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
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
    void save(FhirContext ctx,EpisodeOfCare episode);

    EpisodeOfCare read(FhirContext ctx, IdType theId);

    EpisodeOfCare create(FhirContext ctx,EpisodeOfCare episode, @IdParam IdType theId, @ConditionalUrlParam String theConditional);


    List<EpisodeOfCare> search(FhirContext ctx,

            @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date

            );

    List<EpisodeOfCareEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date

    );
}
