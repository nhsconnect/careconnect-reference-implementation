package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.RiskAssessment;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.RiskAssessmentRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class RiskAssessmentProvider implements ICCResourceProvider {

    @Autowired
    private RiskAssessmentRepository riskAssessmentDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return RiskAssessment.class;
    }

        @Override
        public Long count() {
        return riskAssessmentDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam RiskAssessment riskAssessment, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            RiskAssessment newRiskAssessment = riskAssessmentDao.create(ctx,riskAssessment, theId, theConditional);
            method.setId(newRiskAssessment.getIdElement());
            method.setResource(newRiskAssessment);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam RiskAssessment riskAssessment) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            RiskAssessment newRiskAssessment = riskAssessmentDao.create(ctx,riskAssessment, null,null);
            method.setId(newRiskAssessment.getIdElement());
            method.setResource(newRiskAssessment);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<RiskAssessment> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = RiskAssessment.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = RiskAssessment.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = RiskAssessment.SP_RES_ID) StringParam id
    ) {
        return riskAssessmentDao.search(ctx,patient, identifier,id);
    }

    @Read()
    public RiskAssessment get(@IdParam IdType riskAssessmentId) {

    	resourcePermissionProvider.checkPermission("read");
        RiskAssessment riskAssessment = riskAssessmentDao.read(ctx,riskAssessmentId);

        if ( riskAssessment == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No RiskAssessment/ " + riskAssessmentId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return riskAssessment;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam RiskAssessment resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
