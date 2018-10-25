package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Flag;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Flag;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.FlagRepository;
import uk.nhs.careconnect.ri.database.daointerface.ListRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class FlagProvider implements ICCResourceProvider {


    @Autowired
    private FlagRepository flagDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Flag.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(FlagProvider.class);

    @Override
    public Long count() {
        return flagDao.count();
    }

    @Update
    public MethodOutcome updateFlag(HttpServletRequest theRequest, @ResourceParam Flag flag, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

    try {
        log.info(theId.getId());
        Flag newList = flagDao.create(ctx, flag, theId, theConditional);
        method.setId(newList.getIdElement());
        method.setResource(newList);

    } catch (Exception ex) {

        ProviderResponseLibrary.handleException(method,ex);
    }


        return method;
    }

    @Create
    public MethodOutcome createFlag(HttpServletRequest theRequest, @ResourceParam Flag flag) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
            Flag newFlag = flagDao.create(ctx, flag,null,null);
            method.setId(newFlag.getIdElement());
            method.setResource(newFlag);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

   

    @Read()
    public Flag getList(@IdParam IdType flagId) {

        Flag form = flagDao.read(ctx,flagId);

        if ( form == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No List/ " + flagId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return form;
    }
    @Search
    public List<Flag> searchQuestionnaire(HttpServletRequest theRequest,
                                                           @OptionalParam(name = Flag.SP_IDENTIFIER) TokenParam identifier,
                                                           @OptionalParam(name= Flag.SP_RES_ID) StringParam id,
                                                           @OptionalParam(name = Flag.SP_PATIENT) ReferenceParam patient
    ) {
        return flagDao.searchFlag(ctx, identifier,id,patient);
    }


}
