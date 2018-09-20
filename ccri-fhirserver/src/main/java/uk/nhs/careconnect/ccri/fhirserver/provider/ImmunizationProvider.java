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
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.ImmunizationRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ImmunizationProvider implements ICCResourceProvider {


    @Autowired
    private ImmunizationRepository immunisationDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Immunization.class;
    }

    @Override
    public Long count() {
        return immunisationDao.count();
    }

    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Immunization immunisation, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        try {
        Immunization newImmunisation = immunisationDao.create(ctx,immunisation, theId, theConditional);
        method.setId(newImmunisation.getIdElement());
        method.setResource(newImmunisation);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }
    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Immunization immunisation) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);

        try {
        Immunization newImmunisation = immunisationDao.create(ctx,immunisation, null, null);
        method.setId(newImmunisation.getIdElement());
        method.setResource(newImmunisation);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<Immunization> search(HttpServletRequest theRequest,
                                          @OptionalParam(name = Immunization.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Immunization.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Immunization.SP_STATUS) TokenParam status
            , @OptionalParam(name = Immunization.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Immunization.SP_RES_ID) StringParam resid
    ){
        return immunisationDao.search(ctx,patient,date, status, identifier,resid);
    }

    @Read()
    public Immunization get(@IdParam IdType immunisationId) {

        Immunization immunisation = immunisationDao.read(ctx,immunisationId);

        if ( immunisation == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Immunisation/ " + immunisationId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return immunisation;
    }


}
