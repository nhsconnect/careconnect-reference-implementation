package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.CareTeam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.CareTeamRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class CareTeamProvider implements ICCResourceProvider {

    @Autowired
    private CareTeamRepository careTeamDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CareTeam.class;
    }

        @Override
        public Long count() {
        return careTeamDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam CareTeam careTeam, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            CareTeam newCareTeam = careTeamDao.create(ctx,careTeam, theId, theConditional);
            method.setId(newCareTeam.getIdElement());
            method.setResource(newCareTeam);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam CareTeam careTeam) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            CareTeam newCareTeam = careTeamDao.create(ctx,careTeam, null,null);
            method.setId(newCareTeam.getIdElement());
            method.setResource(newCareTeam);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<CareTeam> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = CareTeam.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CareTeam.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CareTeam.SP_RES_ID) StringParam id
    ) {
        return careTeamDao.search(ctx,patient, identifier,id);
    }

    @Read()
    public CareTeam get(@IdParam IdType careTeamId) {

        CareTeam careTeam = careTeamDao.read(ctx,careTeamId);

        if ( careTeam == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No CareTeam/ " + careTeamId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return careTeam;
    }


}
