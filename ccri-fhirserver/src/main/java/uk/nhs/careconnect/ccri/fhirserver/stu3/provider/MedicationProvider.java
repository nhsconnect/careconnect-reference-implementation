package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.MedicationRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class MedicationProvider implements ICCResourceProvider {


    @Autowired
    private MedicationRepository medicationDao;

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourceTestProvider resourceTestProvider;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    @Override
    public Long count() {
        return medicationDao.count();
    }

    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Medication.class;
    }

    private static final Logger log = LoggerFactory.getLogger(MedicationProvider.class);


    @Read()
    public Medication get(@IdParam IdType prescriptionId) {
    	resourcePermissionProvider.checkPermission("read");
        Medication medication = medicationDao.read(ctx,prescriptionId);

        if ( medication == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Medication/ " + prescriptionId.getIdPart()),
                    OperationOutcome.IssueType.NOTFOUND);
        }

        return medication;
    }

    @Search
    public List<Medication> searchMedication(HttpServletRequest httpRequest
            , @OptionalParam(name = Medication.SP_CODE) TokenParam code
            , @OptionalParam(name = Medication.SP_RES_ID) StringParam resid
    ) {

        return medicationDao.search(ctx,code,resid);


    }

    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Medication medication, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Medication newMedication = medicationDao.create(ctx,medication, theId, theConditional);
            method.setId(newMedication.getIdElement());
            method.setResource(newMedication);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Medication medication) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Medication newMedication = medicationDao.create(ctx,medication, null,null);
            method.setId(newMedication.getIdElement());
            method.setResource(newMedication);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }


    @Validate
    public MethodOutcome testResource(@ResourceParam Medication resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

}
