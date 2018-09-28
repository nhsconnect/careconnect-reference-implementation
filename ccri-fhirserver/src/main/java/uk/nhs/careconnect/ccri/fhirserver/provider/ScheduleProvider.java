package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.ScheduleRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ScheduleProvider implements ICCResourceProvider {


    @Autowired
    private ScheduleRepository scheduleDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Schedule.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return scheduleDao.count();
    }

    @Update
    public MethodOutcome updateSchedule(HttpServletRequest theRequest, @ResourceParam Schedule schedule, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Schedule existingSchedule = scheduleDao.create(ctx, schedule, theId, theConditional);
            method.setId(existingSchedule.getIdElement());
            method.setResource(existingSchedule);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome createSchedule(HttpServletRequest theRequest, @ResourceParam Schedule schedule) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Schedule newSchedule = scheduleDao.create(ctx, schedule,null,null);
            method.setId(newSchedule.getIdElement());
            method.setResource(newSchedule);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }


/*    @Search
    public List<Schedule> searchScheduleBy(HttpServletRequest theRequest,
                                         @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifier,
                                         @OptionalParam(name = Schedule.SP_ACTOR) StringParam name,
                                         @OptionalParam(name= Schedule.SP_TYPE) TokenOrListParam codes,
                                         @OptionalParam(name = Schedule.SP_RES_ID) TokenParam id
    ) {
        return scheduleDao.searchSchedule(ctx, identifier,name,codes,id);
    }*/

/*
    @Search
    public List<Schedule> searchSchedule(HttpServletRequest theRequest,
                                                           @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifier,
                                                           @OptionalParam(name = Schedule.SP_ACTOR) StringParam name,
                                                           @OptionalParam(name= Schedule.SP_TYPE) TokenOrListParam codes,
                                                           @OptionalParam(name = Schedule.SP_RES_ID) StringParam id
    ) {
        return scheduleDao.searchSchedule(ctx, identifier,name,codes,id);
    }
*/

    @Read()
    public Schedule getSchedule(@IdParam IdType scheduleId) {

        Schedule schedule = scheduleDao.read(ctx,scheduleId);

        if ( schedule == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Schedule/ " + scheduleId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return schedule;
    }


}
