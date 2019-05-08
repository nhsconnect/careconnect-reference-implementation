package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.EpisodeOfCareRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class EpisodeOfCareProvider implements ICCResourceProvider {


    @Autowired
    private EpisodeOfCareRepository episodeDao;

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return EpisodeOfCare.class;
    }

    private static final Logger log = LoggerFactory.getLogger(EpisodeOfCareProvider.class);

    @Override
    public Long count() {
        return episodeDao.count();
    }


    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam EpisodeOfCare episode) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            EpisodeOfCare newEpisodeOfCare = episodeDao.create(ctx, episode, null, null);
            method.setId(newEpisodeOfCare.getIdElement());
            method.setResource(newEpisodeOfCare);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam EpisodeOfCare episode, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            EpisodeOfCare newEpisodeOfCare = episodeDao.create(ctx, episode, theId, theConditional);
            method.setId(newEpisodeOfCare.getIdElement());
            method.setResource(newEpisodeOfCare);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
    }




        return method;
    }

    @Search
    public List<EpisodeOfCare> search(HttpServletRequest theRequest,
                                                   @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date
            , @OptionalParam(name = EpisodeOfCare.SP_RES_ID) StringParam resid
            ,@OptionalParam(name= EpisodeOfCare.SP_IDENTIFIER) TokenParam identifier) {
        return episodeDao.search(ctx,patient, date,resid, identifier);
    }

    @Read()
    public EpisodeOfCare get(@IdParam IdType episodeId) {
    	resourcePermissionProvider.checkPermission("read");
        EpisodeOfCare episode = episodeDao.read(ctx,episodeId);

        if ( episode == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No EpisodeOfCare/ " + episodeId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return episode;
    }


    @Validate
    public MethodOutcome testResource(@ResourceParam EpisodeOfCare resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}
