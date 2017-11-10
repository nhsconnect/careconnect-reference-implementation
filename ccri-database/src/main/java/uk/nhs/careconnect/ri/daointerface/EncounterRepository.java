package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;

import java.util.List;

public interface EncounterRepository {
    void save(EncounterEntity encounter);

    Encounter read(IdType theId);

    Encounter create(Encounter encounter, @IdParam IdType theId, @ConditionalUrlParam String theConditional);


    List<Encounter> search(

            @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Encounter.SP_DATE) DateRangeParam date
            ,@OptionalParam(name = Encounter.SP_EPISODEOFCARE) ReferenceParam episode

    );

    List<EncounterEntity> searchEntity(
            @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Encounter.SP_DATE) DateRangeParam date
            ,@OptionalParam(name = Encounter.SP_EPISODEOFCARE) ReferenceParam episode
    );
}
