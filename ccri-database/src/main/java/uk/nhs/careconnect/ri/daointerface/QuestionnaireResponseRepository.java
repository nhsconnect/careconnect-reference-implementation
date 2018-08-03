package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import uk.nhs.careconnect.fhir.OperationOutcomeException;

import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseEntity;

public interface QuestionnaireResponseRepository extends BaseDao<QuestionnaireResponseEntity,QuestionnaireResponse> {

    void save(FhirContext ctx, QuestionnaireResponseEntity form) throws OperationOutcomeException;

    QuestionnaireResponse read(FhirContext ctx, IdType theId);

    QuestionnaireResponseEntity readEntity(FhirContext ctx, IdType theId);

    QuestionnaireResponse create(FhirContext ctx, QuestionnaireResponse questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

}
