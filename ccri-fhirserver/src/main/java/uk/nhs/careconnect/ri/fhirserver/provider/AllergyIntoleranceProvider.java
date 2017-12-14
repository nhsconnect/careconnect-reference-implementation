package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.daointerface.AllergyIntoleranceRepository;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class AllergyIntoleranceProvider implements ICCResourceProvider {

    @Autowired
    private AllergyIntoleranceRepository allergyDao;

    @Autowired
    FhirContext ctx;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }

        @Override
        public Long count() {
        return allergyDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam AllergyIntolerance allergy, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        AllergyIntolerance newAllergyIntolerance = allergyDao.create(ctx,allergy, theId, theConditional);
        method.setId(newAllergyIntolerance.getIdElement());
        method.setResource(newAllergyIntolerance);



        return method;
    }

    @Search
    public List<AllergyIntolerance> search(HttpServletRequest theRequest,
                                           @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus
            , @OptionalParam(name = AllergyIntolerance.SP_IDENTIFIER) TokenParam identifier
    ) {
        return allergyDao.search(ctx,patient, date, clinicalStatus,identifier);
    }

    @Read()
    public AllergyIntolerance get(@IdParam IdType allergyId) {

        AllergyIntolerance allergy = allergyDao.read(ctx,allergyId);

        if ( allergy == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No AllergyIntolerance/ " + allergyId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return allergy;
    }


}
