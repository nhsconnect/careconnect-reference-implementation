package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.*;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.CarePlanRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@Component
public class CarePlanProvider implements ICCResourceProvider {

    @Autowired
    @Lazy
    private CarePlanRepository carePlanDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CarePlan.class;
    }

        @Override
        public Long count() {
        return carePlanDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam CarePlan carePlan, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            CarePlan newCarePlan = carePlanDao.create(ctx,carePlan, theId, theConditional);
            method.setId(newCarePlan.getIdElement());
            method.setResource(newCarePlan);
        } catch (Exception ex) {
               ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam CarePlan carePlan) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            CarePlan newCarePlan = carePlanDao.create(ctx,carePlan, null,null);
            method.setId(newCarePlan.getIdElement());
            method.setResource(newCarePlan);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<Resource> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = CarePlan.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CarePlan.SP_DATE) DateRangeParam date
            , @OptionalParam(name = CarePlan.SP_CATEGORY) TokenOrListParam categories
            , @OptionalParam(name = CarePlan.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CarePlan.SP_RES_ID) StringParam resid
            , @IncludeParam(allow= {
            "CarePlan:subject"
            ,"CarePlan:supportingInformation"
            , "*"}) Set<Include> includes
    ) {
        return carePlanDao.search(ctx,patient, date, categories,identifier,resid, includes);
    }

    @Read()
    public CarePlan get(@IdParam IdType carePlanId) {

        CarePlan carePlan = carePlanDao.read(ctx,carePlanId);

        if ( carePlan == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No CarePlan/ " + carePlanId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return carePlan;
    }


}
