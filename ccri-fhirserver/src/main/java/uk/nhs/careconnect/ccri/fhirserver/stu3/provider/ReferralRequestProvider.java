package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ReferralRequestRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ReferralRequestProvider implements ICCResourceProvider {


    @Autowired
    private ReferralRequestRepository referralDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ReferralRequest.class;
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
        return referralDao.count();
    }

    @Update
    public MethodOutcome updateReferralRequest(HttpServletRequest theRequest, @ResourceParam ReferralRequest referral, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            ReferralRequest newReferralRequest = referralDao.create(ctx, referral, theId, theConditional);
            method.setId(newReferralRequest.getIdElement());
            method.setResource(newReferralRequest);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Create
    public MethodOutcome createReferralRequest(HttpServletRequest theRequest, @ResourceParam ReferralRequest referral) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            ReferralRequest newReferralRequest = referralDao.create(ctx, referral,null,null);
            method.setId(newReferralRequest.getIdElement());
            method.setResource(newReferralRequest);

        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Search
    public List<ReferralRequest> searchReferralRequest(HttpServletRequest theRequest,
               @OptionalParam(name = ReferralRequest.SP_IDENTIFIER) TokenParam identifier,
               @OptionalParam(name = ReferralRequest.SP_TYPE) TokenOrListParam codes,
               @OptionalParam(name = ReferralRequest.SP_RES_ID) StringParam id,
               @OptionalParam(name = ReferralRequest.SP_PATIENT) ReferenceParam patient
    ) {
        return referralDao.searchReferralRequest(ctx, identifier,codes,id,patient);
    }

    @Read()
    public ReferralRequest getReferralRequest(@IdParam IdType referralId) {
    	resourcePermissionProvider.checkPermission("read");
        ReferralRequest referral = referralDao.read(ctx,referralId);

        if ( referral == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No ReferralRequest/ " + referralId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return referral;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam ReferralRequest resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }


}
