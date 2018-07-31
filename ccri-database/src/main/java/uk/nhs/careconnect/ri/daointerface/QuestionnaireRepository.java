package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Questionnaire;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireEntity;

import java.util.List;

public interface QuestionnaireRepository extends BaseDao<QuestionnaireEntity,Questionnaire> {
    void save(FhirContext ctx, QuestionnaireEntity questionnaire) throws OperationOutcomeException;

    Questionnaire read(FhirContext ctx, IdType theId);

    QuestionnaireEntity readEntity(FhirContext ctx, IdType theId);

    Questionnaire create(FhirContext ctx, Questionnaire questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

}
