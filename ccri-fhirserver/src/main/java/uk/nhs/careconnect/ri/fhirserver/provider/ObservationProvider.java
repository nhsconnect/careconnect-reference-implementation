package uk.nhs.careconnect.ri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.daointerface.ObservationRepository;
import uk.nhs.careconnect.ri.lib.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Component
public class ObservationProvider implements ICCResourceProvider {


    private static final List<String> MANDATORY_PARAM_NAMES = Arrays.asList("patientNHSNumber", "recordSection");


    @Autowired
    private ObservationRepository observationDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(ObservationProvider.class);

    @Override
    public Long count() {
        return observationDao.count();
    }

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Observation observation, @IdParam IdType theId,@ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
            Observation newObservation = observationDao.save(ctx,observation,theId,theConditional);
            method.setId(newObservation.getIdElement());
            method.setResource(newObservation);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Observation observation) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
            Observation newObservation = observationDao.save(ctx, observation, null, null);
            method.setId(newObservation.getIdElement());
            method.setResource(newObservation);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Read
    public Observation getObservationById(@IdParam IdType internalId) {
        Observation observation = observationDao.read(ctx,internalId);
        if (observation == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No observation found for ID: " + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }
        return observation;
    }

    @Search
    public List<Observation> search(HttpServletRequest theRequest,

                                    @OptionalParam(name= Observation.SP_CATEGORY) TokenParam category,
                                    @OptionalParam(name= Observation.SP_CODE) TokenOrListParam codes,
                                    @OptionalParam(name= Observation.SP_DATE) DateRangeParam effectiveDate,
                                    @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient,
                                    @OptionalParam(name = Observation.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Observation.SP_RES_ID) TokenParam resid
            ,@OptionalParam(name = Observation.SP_SUBJECT) ReferenceParam subject
                                       ) {
        return observationDao.search(ctx,category, codes, effectiveDate,patient, identifier,resid,subject);
    }



}
