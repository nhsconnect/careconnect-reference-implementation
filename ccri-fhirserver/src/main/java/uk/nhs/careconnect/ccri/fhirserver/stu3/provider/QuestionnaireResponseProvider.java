package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.QuestionnaireResponseRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@Component
public class QuestionnaireResponseProvider implements ICCResourceProvider {


    @Autowired
    private QuestionnaireResponseRepository formDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return QuestionnaireResponse.class;
    }

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireResponseProvider.class);

    @Override
    public Long count() {
        return formDao.count();
    }

    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam QuestionnaireResponse form, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

        resourcePermissionProvider.checkPermission("update");


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {

        /* Moved to Validation interceptor
        MethodOutcome testMethod = resourceTestProvider.testResource(form,null,null);
        if (!resourceTestProvider.pass(testMethod)) {
            throw new ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException(ctx, testMethod.getOperationOutcome());
        }*/


            QuestionnaireResponse newForm = formDao.create(ctx, form, theId, theConditional);
            method.setId(newForm.getIdElement());
            method.setResource(newForm);

        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method, ex);
        }


        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam QuestionnaireResponse form) {

        resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {

            QuestionnaireResponse newForm = formDao.create(ctx, form, null, null);
            method.setId(newForm.getIdElement());
            method.setResource(newForm);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }


    @Read()
    public QuestionnaireResponse getQuestionnaireResponse(@IdParam IdType formId) {
        resourcePermissionProvider.checkPermission("read");
        QuestionnaireResponse form = formDao.read(ctx, formId);

        if (form == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No QuestionnaireResponse/ " + formId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return form;
    }

    @Search
    public List<Resource> searchQuestionnaire(HttpServletRequest theRequest,
                                              @OptionalParam(name = QuestionnaireResponse.SP_IDENTIFIER) TokenParam identifier,
                                              @OptionalParam(name = QuestionnaireResponse.SP_RES_ID) StringParam id,
                                              @OptionalParam(name = QuestionnaireResponse.SP_QUESTIONNAIRE) ReferenceParam questionnaire,
                                              @OptionalParam(name = QuestionnaireResponse.SP_PATIENT) ReferenceParam patient,
                                              @IncludeParam(allow = {"*"}) Set<Include> includes
    ) {
        return formDao.searchQuestionnaireResponse(ctx, identifier, id, questionnaire, patient, includes);
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam QuestionnaireResponse resource,
                                      @Validate.Mode ValidationModeEnum theMode,
                                      @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource, theMode, theProfile);
    }

}
