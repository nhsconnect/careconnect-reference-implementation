package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Claim;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ClaimRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ClaimProvider implements ICCResourceProvider {

    @Autowired
    private ClaimRepository claimDao;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Claim.class;
    }

        @Override
        public Long count() {
        return claimDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Claim claim, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
    	
    	resourcePermissionProvider.checkPermission("update");
    	
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Claim newClaim = claimDao.create(ctx,claim, theId, theConditional);
            method.setId(newClaim.getIdElement());
            method.setResource(newClaim);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Claim claim) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Claim newClaim = claimDao.create(ctx,claim, null,null);
            method.setId(newClaim.getIdElement());
            method.setResource(newClaim);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<Resource> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = Claim.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Claim.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Claim.SP_RES_ID) StringParam id
            , @OptionalParam(name = Claim.SP_USE) TokenParam use
            , @OptionalParam(name = "status") TokenParam status
    ) {
        return claimDao.search(ctx,patient, identifier,id, use, status);
    }

    @Read()
    public Claim get(@IdParam IdType claimId) {

    	resourcePermissionProvider.checkPermission("read");
        Claim claim = claimDao.read(ctx,claimId);

        if ( claim == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Claim/ " + claimId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return claim;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Claim resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @OptionalParam(name = "profile") @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}
