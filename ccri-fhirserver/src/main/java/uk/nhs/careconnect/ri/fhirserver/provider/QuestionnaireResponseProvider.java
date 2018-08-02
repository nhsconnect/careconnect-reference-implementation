package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.QuestionnaireRepository;
import uk.nhs.careconnect.ri.daointerface.QuestionnaireResponseRepository;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;

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

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireResponseProvider.class);

    @Override
    public Long count() {
        return formDao.count();
    }

    @Update
    public MethodOutcome updateQuestionnaireResponse(HttpServletRequest theRequest, @ResourceParam QuestionnaireResponse form, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

    try {
        QuestionnaireResponse newForm = formDao.create(ctx, form, theId, theConditional);
        method.setId(newForm.getIdElement());
        method.setResource(newForm);

    } catch (Exception ex) {

        if (ex instanceof OperationOutcomeException) {
            OperationOutcomeException outcomeException = (OperationOutcomeException) ex;
            method.setOperationOutcome(outcomeException.getOutcome());
            method.setCreated(false);
        } else {
            log.error(ex.getMessage());
            method.setCreated(false);
            method.setOperationOutcome(OperationOutcomeFactory.createOperationOutcome(ex.getMessage()));
        }
    }


        return method;
    }

    @Create
    public MethodOutcome createQuestionnaireResponse(HttpServletRequest theRequest, @ResourceParam QuestionnaireResponse form) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
        QuestionnaireResponse newForm = formDao.create(ctx, form,null,null);
        method.setId(newForm.getIdElement());
        method.setResource(newForm);
        } catch (Exception ex) {

            if (ex instanceof OperationOutcomeException) {
                OperationOutcomeException outcomeException = (OperationOutcomeException) ex;
                method.setOperationOutcome(outcomeException.getOutcome());
                method.setCreated(false);
            } else {
                log.error(ex.getMessage());
                method.setCreated(false);
                method.setOperationOutcome(OperationOutcomeFactory.createOperationOutcome(ex.getMessage()));
            }
        }

        return method;
    }

   

    @Read()
    public QuestionnaireResponse getQuestionnaireResponse(@IdParam IdType formId) {

        QuestionnaireResponse form = formDao.read(ctx,formId);

        if ( form == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No QuestionnaireResponse/ " + formId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return form;
    }


}
