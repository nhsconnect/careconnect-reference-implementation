package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.daointerface.MedicationRequestRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class MedicationRequestProvider implements ICCResourceProvider {


    @Autowired
    private MedicationRequestRepository prescriptionDao;

    @Autowired
    FhirContext ctx;

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


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        MedicationRequest newMedicationRequest = prescriptionDao.create(ctx,prescription, theId, theConditional);
        method.setId(newMedicationRequest.getIdElement());
        method.setResource(newMedicationRequest);



        return method;
    }

    @Search
    public List<MedicationRequest> search(HttpServletRequest theRequest,
                                          @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationRequest.SP_CODE) TokenParam code
            , @OptionalParam(name = MedicationRequest.SP_AUTHOREDON) DateRangeParam dateWritten
            , @OptionalParam(name = MedicationRequest.SP_STATUS) TokenParam status
                                          ) {
        return prescriptionDao.search(ctx,patient, code, dateWritten, status);
    }

    @Read()
    public MedicationRequest get(@IdParam IdType prescriptionId) {

        MedicationRequest prescription = prescriptionDao.read(ctx,prescriptionId);

        if ( prescription == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No MedicationRequest/ " + prescriptionId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return prescription;
    }


}
