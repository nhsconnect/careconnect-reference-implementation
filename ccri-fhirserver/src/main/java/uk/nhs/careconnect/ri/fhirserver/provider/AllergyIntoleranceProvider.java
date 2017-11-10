package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.common.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.daointerface.AllergyIntoleranceRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class AllergyIntoleranceProvider implements IResourceProvider {


    @Autowired
    private AllergyIntoleranceRepository allergyDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam AllergyIntolerance allergy, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        AllergyIntolerance newAllergyIntolerance = allergyDao.create(allergy, theId, theConditional);
        method.setId(newAllergyIntolerance.getIdElement());
        method.setResource(newAllergyIntolerance);



        return method;
    }

    @Search
    public List<AllergyIntolerance> search(HttpServletRequest theRequest,
                                           @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus
    ) {
        return allergyDao.search(patient, date, clinicalStatus);
    }

    @Read()
    public AllergyIntolerance get(@IdParam IdType allergyId) {

        AllergyIntolerance allergy = allergyDao.read(allergyId);

        if ( allergy == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No AllergyIntolerance/ " + allergyId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return allergy;
    }


}
