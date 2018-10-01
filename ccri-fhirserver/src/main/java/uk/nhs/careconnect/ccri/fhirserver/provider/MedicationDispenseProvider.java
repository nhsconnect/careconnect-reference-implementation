package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.MedicationDispenseRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class MedicationDispenseProvider implements ICCResourceProvider {


    @Autowired
    private MedicationDispenseRepository dispenseDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return dispenseDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationDispense.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam MedicationDispense dispense, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        MedicationDispense newMedicationDispense = dispenseDao.create(ctx,dispense, theId, theConditional);
        method.setId(newMedicationDispense.getIdElement());
        method.setResource(newMedicationDispense);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam MedicationDispense dispense) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        MedicationDispense newMedicationDispense = dispenseDao.create(ctx,dispense, null,null);
        method.setId(newMedicationDispense.getIdElement());
        method.setResource(newMedicationDispense);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Search
    public List<MedicationDispense> search(HttpServletRequest theRequest,
                                           @OptionalParam(name = MedicationDispense.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationDispense.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationDispense.SP_RES_ID) StringParam id
            , @OptionalParam(name = MedicationDispense.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationDispense.SP_CODE) TokenParam code
            , @OptionalParam(name= MedicationDispense.SP_MEDICATION) ReferenceParam medication
                                          ) {
        return dispenseDao.search(ctx,patient, status, id, identifier,code, medication );
    }

    @Read()
    public MedicationDispense get(@IdParam IdType dispenseId) {

        MedicationDispense dispense = dispenseDao.read(ctx,dispenseId);

        if ( dispense == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No MedicationDispense/ " + dispenseId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return dispense;
    }


}
