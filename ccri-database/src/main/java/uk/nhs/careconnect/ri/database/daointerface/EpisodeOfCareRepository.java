package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;

import java.util.List;

public interface EpisodeOfCareRepository extends BaseRepository<EpisodeOfCareEntity,EpisodeOfCare> {
    void save(FhirContext ctx,EpisodeOfCare episode) throws OperationOutcomeException;

    EpisodeOfCare read(FhirContext ctx, IdType theId);

    EpisodeOfCare create(FhirContext ctx,EpisodeOfCare episode, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;



    List<EpisodeOfCare> search(FhirContext ctx,

            @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date
            ,@OptionalParam(name= EpisodeOfCare.SP_RES_ID) StringParam id
            );

    List<EpisodeOfCareEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date
            ,@OptionalParam(name= EpisodeOfCare.SP_RES_ID) StringParam id

    );
}
