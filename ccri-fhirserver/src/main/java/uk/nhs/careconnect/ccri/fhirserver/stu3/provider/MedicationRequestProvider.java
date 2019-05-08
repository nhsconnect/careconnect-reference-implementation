package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.MedicationRequestRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class MedicationRequestProvider implements ICCResourceProvider {


    @Autowired
    private MedicationRequestRepository prescriptionDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(MedicationRequestProvider.class);

    @Override
    public Long count() {
        return prescriptionDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationRequest.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam MedicationRequest prescription, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        MedicationRequest newMedicationRequest = prescriptionDao.create(ctx,prescription, theId, theConditional);
        method.setId(newMedicationRequest.getIdElement());
        method.setResource(newMedicationRequest);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam MedicationRequest prescription) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        MedicationRequest newMedicationRequest = prescriptionDao.create(ctx,prescription, null,null);
        method.setId(newMedicationRequest.getIdElement());
        method.setResource(newMedicationRequest);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Search
    public List<MedicationRequest> search(HttpServletRequest theRequest,
                                          @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationRequest.SP_CODE) TokenParam code
            , @OptionalParam(name = MedicationRequest.SP_AUTHOREDON) DateRangeParam dateWritten
            , @OptionalParam(name = MedicationRequest.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationRequest.SP_RES_ID) StringParam resid
            , @OptionalParam(name = MedicationRequest.SP_IDENTIFIER)  TokenParam identifierCode
            , @OptionalParam(name = MedicationRequest.SP_MEDICATION) ReferenceParam medication
                                          ) {
        return prescriptionDao.search(ctx,patient, code, dateWritten, status,identifierCode,resid,medication);
    }

    @Read()
    public MedicationRequest get(@IdParam IdType prescriptionId) {
    	resourcePermissionProvider.checkPermission("read");
        MedicationRequest prescription = prescriptionDao.read(ctx,prescriptionId);

        if ( prescription == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No MedicationRequest/ " + prescriptionId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return prescription;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam MedicationRequest resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}
