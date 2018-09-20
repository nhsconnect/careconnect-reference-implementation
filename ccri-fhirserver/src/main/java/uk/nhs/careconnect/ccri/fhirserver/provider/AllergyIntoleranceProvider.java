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
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.AllergyIntoleranceRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class AllergyIntoleranceProvider implements ICCResourceProvider {

    @Autowired
    private AllergyIntoleranceRepository allergyDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

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

        try {
        AllergyIntolerance newAllergyIntolerance = allergyDao.create(ctx,allergy, theId, theConditional);
        method.setId(newAllergyIntolerance.getIdElement());
        method.setResource(newAllergyIntolerance);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam AllergyIntolerance allergy) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        AllergyIntolerance newAllergyIntolerance = allergyDao.create(ctx,allergy, null,null);
        method.setId(newAllergyIntolerance.getIdElement());
        method.setResource(newAllergyIntolerance);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Search
    public List<AllergyIntolerance> search(HttpServletRequest theRequest,
                                           @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus
            , @OptionalParam(name = AllergyIntolerance.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = AllergyIntolerance.SP_RES_ID) StringParam resid
    ) {
        return allergyDao.search(ctx,patient, date, clinicalStatus,identifier,resid);
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
