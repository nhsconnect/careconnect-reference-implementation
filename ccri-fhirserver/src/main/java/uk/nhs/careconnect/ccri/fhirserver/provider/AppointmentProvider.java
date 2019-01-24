package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.AppointmentRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class AppointmentProvider implements ICCResourceProvider {


    @Autowired
    private AppointmentRepository appointmentDao;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Appointment.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return appointmentDao.count();
    }

    @Update
    public MethodOutcome updateAppointment(HttpServletRequest theRequest, @ResourceParam Appointment appointment, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
    	
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Appointment newAppointment = appointmentDao.create(ctx, appointment, theId, theConditional);
            method.setId(newAppointment.getIdElement());
            method.setResource(newAppointment);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome createAppointment(HttpServletRequest theRequest, @ResourceParam Appointment appointment) {

    	resourcePermissionProvider.checkPermission("create");
    	
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Appointment newAppointment = appointmentDao.create(ctx, appointment,null,null);
            method.setId(newAppointment.getIdElement());
            method.setResource(newAppointment);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<Appointment> searchAppointment(HttpServletRequest theRequest,
                                                           @OptionalParam(name = Appointment.SP_IDENTIFIER) TokenParam identifier,
                                                           @OptionalParam(name = Appointment.SP_APPOINTMENT_TYPE) StringParam appointmentType,
                                                           @OptionalParam(name = Appointment.SP_STATUS) StringParam status,
                                                           @OptionalParam(name = Appointment.SP_RES_ID) StringParam id
    ) {
        return appointmentDao.searchAppointment(ctx, identifier,appointmentType,status,id);
    }

    @Read()
    public Appointment getAppointment(@IdParam IdType serviceId) {
    	resourcePermissionProvider.checkPermission("read");
    	
        Appointment appointment = appointmentDao.read(ctx,serviceId);

        if ( appointment == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Appointment/ " + serviceId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return appointment;
    }


    @Validate
    public MethodOutcome testResource(@ResourceParam Appointment resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}
