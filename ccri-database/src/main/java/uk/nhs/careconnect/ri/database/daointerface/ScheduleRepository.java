package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;

import java.util.List;

public interface ScheduleRepository extends BaseRepository<ScheduleEntity,Schedule> {
    void save(FhirContext ctx, ScheduleEntity location) throws OperationOutcomeException;

    Schedule read(FhirContext ctx, IdType theId);

    ScheduleEntity readEntity(FhirContext ctx, IdType theId);

    Schedule create(FhirContext ctx, Schedule location, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Schedule> searchSchedule(FhirContext ctx,
                                              @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifier,
                                              @OptionalParam(name = Schedule.SP_ACTOR) StringParam actor,
                                              @OptionalParam(name = Schedule.SP_TYPE) TokenOrListParam codes,
                                              @OptionalParam(name = Schedule.SP_RES_ID) StringParam id
    );

    List<ScheduleEntity> searchScheduleEntity(FhirContext ctx,
                                              @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifier,
                                              @OptionalParam(name = Schedule.SP_ACTOR) StringParam actor,
                                              @OptionalParam(name = Schedule.SP_TYPE) TokenOrListParam codes,
                                              @OptionalParam(name = Schedule.SP_RES_ID) StringParam id
    );
}
