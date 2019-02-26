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
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationAdministration;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.MedicationAdministrationRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class MedicationAdministrationProvider implements ICCResourceProvider {


    @Autowired
    private MedicationAdministrationRepository administrationDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return administrationDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationAdministration.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam MedicationAdministration administration, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        MedicationAdministration newMedicationAdministration = administrationDao.create(ctx,administration, theId, theConditional);
        method.setId(newMedicationAdministration.getIdElement());
        method.setResource(newMedicationAdministration);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam MedicationAdministration administration) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        MedicationAdministration newMedicationAdministration = administrationDao.create(ctx,administration, null,null);
        method.setId(newMedicationAdministration.getIdElement());
        method.setResource(newMedicationAdministration);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Search
    public List<MedicationAdministration> search(HttpServletRequest theRequest,
                                           @OptionalParam(name = MedicationAdministration.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationAdministration.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationAdministration.SP_RES_ID) StringParam id
            , @OptionalParam(name = MedicationAdministration.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationAdministration.SP_CODE) TokenParam code
            , @OptionalParam(name= MedicationAdministration.SP_MEDICATION) ReferenceParam medication
                                          ) {
        return administrationDao.search(ctx,patient, status, id, identifier,code, medication );
    }

    @Read()
    public MedicationAdministration get(@IdParam IdType administrationId) {
    	resourcePermissionProvider.checkPermission("read");
        MedicationAdministration administration = administrationDao.read(ctx,administrationId);

        if ( administration == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No MedicationAdministration/ " + administrationId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return administration;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam MedicationAdministration resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
