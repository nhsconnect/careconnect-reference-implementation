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
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.MedicationStatementRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class MedicationStatementProvider implements ICCResourceProvider {


    @Autowired
    private MedicationStatementRepository statementDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationStatement.class;
    }

    @Override
    public Long count() {
        return statementDao.count();
    }


    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam MedicationStatement statement) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            MedicationStatement newMedicationStatement = statementDao.create(ctx,statement, null,null);
            method.setId(newMedicationStatement.getIdElement());
            method.setResource(newMedicationStatement);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam MedicationStatement statement, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        MedicationStatement newMedicationStatement = statementDao.create(ctx,statement, theId, theConditional);
        method.setId(newMedicationStatement.getIdElement());
        method.setResource(newMedicationStatement);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Search
    public List<MedicationStatement> search(HttpServletRequest theRequest,
                                            @OptionalParam(name = MedicationStatement.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationStatement.SP_EFFECTIVE) DateRangeParam effectiveDate
            , @OptionalParam(name = MedicationStatement.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationStatement.SP_RES_ID) StringParam resid
            , @OptionalParam(name = MedicationStatement.SP_IDENTIFIER) TokenParam identifier
    ) {
        return statementDao.search(ctx,patient, effectiveDate, status,resid, identifier);
    }

    @Read()
    public MedicationStatement get(@IdParam IdType statementId) {
    	resourcePermissionProvider.checkPermission("read");
        MedicationStatement statement = statementDao.read(ctx,statementId);

        if ( statement == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No MedicationStatement/ " + statementId.getIdPart()),
                    OperationOutcome.IssueType.NOTFOUND);
        }

        return statement;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam MedicationStatement resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}
