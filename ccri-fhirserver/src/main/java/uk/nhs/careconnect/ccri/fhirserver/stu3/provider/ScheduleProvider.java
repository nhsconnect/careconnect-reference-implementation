package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
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
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ScheduleRepository;

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
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return scheduleDao.count();
    }

    @Update
    public MethodOutcome updateSchedule(HttpServletRequest theRequest, @ResourceParam Schedule schedule, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
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

    	resourcePermissionProvider.checkPermission("create");
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





    @Search
    public List<Schedule> searchSchedule(HttpServletRequest theRequest,
                                                           @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifier,
                                                          // @OptionalParam(name = Schedule.SP_ACTOR) StringParam name,
                                                          // @OptionalParam(name= Schedule.SP_TYPE) TokenOrListParam codes,
                                                           @OptionalParam(name = Schedule.SP_RES_ID) StringParam id
    ) {
        return scheduleDao.searchSchedule(ctx, identifier,id);
    }


    @Read()
    public Schedule getSchedule(@IdParam IdType scheduleId) {
    	resourcePermissionProvider.checkPermission("read");
        Schedule schedule = scheduleDao.read(ctx,scheduleId);

        if ( schedule == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Schedule/ " + scheduleId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return schedule;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Schedule resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
