package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.ClinicalImpression;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ClinicalImpressionRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ClinicalImpressionProvider implements ICCResourceProvider {

	@Autowired
    private ClinicalImpressionRepository impressionDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ClinicalImpression.class;
    }

        @Override
        public Long count() {
        return impressionDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam ClinicalImpression impression, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            ClinicalImpression newClinicalImpression = impressionDao.create(ctx,impression, theId, theConditional);
            method.setId(newClinicalImpression.getIdElement());
            method.setResource(newClinicalImpression);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam ClinicalImpression impression) {

    	resourcePermissionProvider.checkPermission("read");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            ClinicalImpression newClinicalImpression = impressionDao.create(ctx,impression, null,null);
            method.setId(newClinicalImpression.getIdElement());
            method.setResource(newClinicalImpression);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<ClinicalImpression> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = ClinicalImpression.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = ClinicalImpression.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = ClinicalImpression.SP_RES_ID) StringParam id
    ) {
        return impressionDao.search(ctx,patient, identifier,id);
    }

    @Read()
    public ClinicalImpression get(@IdParam IdType impressionId) {
    	resourcePermissionProvider.checkPermission("read");
        ClinicalImpression impression = impressionDao.read(ctx,impressionId);

        if ( impression == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No ClinicalImpression/ " + impressionId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return impression;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam ClinicalImpression resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
