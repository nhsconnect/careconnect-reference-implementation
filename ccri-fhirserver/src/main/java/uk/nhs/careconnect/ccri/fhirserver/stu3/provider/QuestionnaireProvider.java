package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.QuestionnaireRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class QuestionnaireProvider implements ICCResourceProvider {


    @Autowired
    private QuestionnaireRepository questionnaireDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Questionnaire.class;
    }

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return questionnaireDao.count();
    }

    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Questionnaire questionnaire,
                                @IdParam IdType theId,
                                @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

        resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Questionnaire newQuestionnaire = questionnaireDao.create(ctx, questionnaire, theId, theConditional);
            method.setId(newQuestionnaire.getIdElement());
            method.setResource(newQuestionnaire);

        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method, ex);
        }


        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Questionnaire questionnaire) {

        resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
            Questionnaire newQuestionnaire = questionnaireDao.create(ctx, questionnaire, null, null);
            method.setId(newQuestionnaire.getIdElement());
            method.setResource(newQuestionnaire);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }


    @Read()
    public Questionnaire get(@IdParam IdType questionnaireId) {
        resourcePermissionProvider.checkPermission("read");
        Questionnaire questionnaire = questionnaireDao.read(ctx, questionnaireId);

        if (questionnaire == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Questionnaire/ " + questionnaireId.getIdPart()),
                    OperationOutcome.IssueType.NOTFOUND);
        }

        return questionnaire;
    }

    @Search
    public List<Questionnaire> search(HttpServletRequest theRequest,
                                      @OptionalParam(name = Questionnaire.SP_IDENTIFIER) TokenParam identifier,
                                      @OptionalParam(name = Questionnaire.SP_RES_ID) StringParam id,
                                      @OptionalParam(name = Questionnaire.SP_CODE) TokenOrListParam codes,
                                      @OptionalParam(name = Questionnaire.SP_URL) UriParam url,
                                      @OptionalParam(name = Questionnaire.SP_NAME) StringParam name
    ) {
        return questionnaireDao.search(ctx, identifier, id, codes, url, name);
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Questionnaire resource,
                                      @Validate.Mode ValidationModeEnum theMode,
                                      @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource, theMode, theProfile);
    }

}
