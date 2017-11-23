package uk.nhs.careconnect.ri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.daointerface.ObservationRepository;

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

    @Override
    public Long count() {
        return observationDao.count();
    }

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }


    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Observation observation) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        observation = observationDao.save(ctx,observation);

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
                                    @OptionalParam(name= Observation.SP_CODE) TokenParam code,
                                    @OptionalParam(name= Observation.SP_DATE) DateRangeParam effectiveDate,
                                    @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient
                                       ) {
        return observationDao.search(ctx,category, code, effectiveDate,patient);
    }



}
