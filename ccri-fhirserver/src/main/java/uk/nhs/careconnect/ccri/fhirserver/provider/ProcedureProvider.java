package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.ProcedureRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ProcedureProvider implements ICCResourceProvider {


    @Autowired
    private ProcedureRepository procedureDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Procedure.class;
    }

    @Override
    public Long count() {
        return procedureDao.count();
    }

    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Procedure procedure, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        Procedure newProcedure = procedureDao.create(ctx,procedure, theId, theConditional);
        method.setId(newProcedure.getIdElement());
        method.setResource(newProcedure);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Procedure procedure) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        Procedure newProcedure = procedureDao.create(ctx,procedure, null,null);
        method.setId(newProcedure.getIdElement());
        method.setResource(newProcedure);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Search
    public List<Procedure> search(HttpServletRequest theRequest,
                                  @OptionalParam(name = Procedure.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Procedure.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Procedure.SP_SUBJECT) ReferenceParam subject
            , @OptionalParam(name = Procedure.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Procedure.SP_RES_ID) StringParam resid
                                  ) {
        return procedureDao.search(ctx, patient, date, subject,identifier,resid);
    }

    @Read()
    public Procedure get(@IdParam IdType procedureId) {

        Procedure procedure = procedureDao.read(ctx, procedureId);

        if ( procedure == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Procedure/ " + procedureId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return procedure;
    }


}
