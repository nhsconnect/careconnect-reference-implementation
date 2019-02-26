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
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.DiagnosticReportRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class DiagnosticReportProvider implements ICCResourceProvider {



    @Autowired
    private DiagnosticReportRepository diagnosticReportDao;

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourceTestProvider resourceTestProvider;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return diagnosticReportDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return DiagnosticReport.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam DiagnosticReport diagnosticReport, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);

        try {
        DiagnosticReport newDiagnosticReport = diagnosticReportDao.create(ctx,diagnosticReport, theId, theConditional);
        method.setId(newDiagnosticReport.getIdElement());
        method.setResource(newDiagnosticReport);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam DiagnosticReport diagnosticReport) {

    	resourcePermissionProvider.checkPermission("create");
    	MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);

        try {
        DiagnosticReport newDiagnosticReport = diagnosticReportDao.create(ctx,diagnosticReport, null,null);
        method.setId(newDiagnosticReport.getIdElement());
        method.setResource(newDiagnosticReport);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<DiagnosticReport> search(HttpServletRequest theRequest,
                                  @OptionalParam(name = DiagnosticReport.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = DiagnosticReport.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = DiagnosticReport.SP_RES_ID) StringParam resid
                                  ) {
        return diagnosticReportDao.search(ctx,patient,identifier,resid);
    }

    @Read()
    public DiagnosticReport get(@IdParam IdType diagnosticReportId) {
    	resourcePermissionProvider.checkPermission("read");
        DiagnosticReport diagnosticReport = diagnosticReportDao.read(ctx,diagnosticReportId);

        if ( diagnosticReport == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No DiagnosticReport/ " + diagnosticReportId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return diagnosticReport;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam DiagnosticReport resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
