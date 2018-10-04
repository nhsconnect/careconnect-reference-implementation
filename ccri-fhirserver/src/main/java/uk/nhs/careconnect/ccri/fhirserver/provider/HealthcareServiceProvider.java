package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.HealthcareServiceRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class HealthcareServiceProvider implements ICCResourceProvider {


    @Autowired
    private HealthcareServiceRepository serviceDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return HealthcareService.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return serviceDao.count();
    }

    @Update
    public MethodOutcome updateHealthcareService(HttpServletRequest theRequest, @ResourceParam HealthcareService service, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            HealthcareService newHealthcareService = serviceDao.create(ctx, service, theId, theConditional);
            method.setId(newHealthcareService.getIdElement());
            method.setResource(newHealthcareService);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome createHealthcareService(HttpServletRequest theRequest, @ResourceParam HealthcareService service) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            HealthcareService newHealthcareService = serviceDao.create(ctx, service,null,null);
            method.setId(newHealthcareService.getIdElement());
            method.setResource(newHealthcareService);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<Resource> searchHealthcareService(HttpServletRequest theRequest,
                                                  @OptionalParam(name = HealthcareService.SP_IDENTIFIER) TokenParam identifier,
                                                  @OptionalParam(name = HealthcareService.SP_NAME) StringParam name,
                                                  @OptionalParam(name= HealthcareService.SP_TYPE) TokenOrListParam codes,
                                                  @OptionalParam(name = HealthcareService.SP_RES_ID) StringParam id,
                                                  @OptionalParam(name = HealthcareService.SP_ORGANIZATION) ReferenceParam organisation,
                                                  @IncludeParam(reverse=true, allow = {"Slot", "*"}) Set<Include> reverseIncludes
    ) {
        return serviceDao.searchHealthcareService(ctx, identifier,name,codes,id,organisation,reverseIncludes);
    }

    @Read()
    public HealthcareService getHealthcareService(@IdParam IdType serviceId) {

        HealthcareService service = serviceDao.read(ctx,serviceId);

        if ( service == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No HealthcareService/ " + serviceId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return service;
    }


}
