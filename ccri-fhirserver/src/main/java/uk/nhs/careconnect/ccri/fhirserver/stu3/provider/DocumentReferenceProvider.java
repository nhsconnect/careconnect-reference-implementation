package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.*;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.DocumentReferenceRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class DocumentReferenceProvider implements ICCResourceProvider {



    @Autowired
    private DocumentReferenceRepository documentReferenceDao;

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;
    
    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return documentReferenceDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return DocumentReference.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam DocumentReference documentReference, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        try {
        DocumentReference newDocumentReference = documentReferenceDao.create(ctx,documentReference, theId, theConditional);
        method.setId(newDocumentReference.getIdElement());
        method.setResource(newDocumentReference);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam DocumentReference documentReference) {
    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);

        try {
        DocumentReference newDocumentReference = documentReferenceDao.create(ctx,documentReference, null,null);
        method.setId(newDocumentReference.getIdElement());
        method.setResource(newDocumentReference);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<DocumentReference> search(HttpServletRequest theRequest,
              @OptionalParam(name = DocumentReference.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = DocumentReference.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = DocumentReference.SP_RES_ID) StringParam resid
            , @OptionalParam(name = DocumentReference.SP_TYPE) TokenOrListParam type
            , @OptionalParam(name = DocumentReference.SP_PERIOD)DateRangeParam dateRange
            , @OptionalParam(name = DocumentReference.SP_SETTING) TokenParam setting
                                  ) {
        return documentReferenceDao.search(ctx,patient,identifier,resid,type,dateRange,setting);
    }

    @Read()
    public DocumentReference get(@IdParam IdType documentReferenceId) {
    	resourcePermissionProvider.checkPermission("read");
        DocumentReference documentReference = documentReferenceDao.read(ctx,documentReferenceId);

        if ( documentReference == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No DocumentReference/ " + documentReferenceId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return documentReference;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam DocumentReference resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
