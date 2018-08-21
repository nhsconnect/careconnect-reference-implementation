package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import uk.nhs.careconnect.fhir.OperationOutcomeException;

import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseEntity;

import java.util.List;

public interface QuestionnaireResponseRepository extends BaseRepository<QuestionnaireResponseEntity,QuestionnaireResponse> {

    void save(FhirContext ctx, QuestionnaireResponseEntity form) throws OperationOutcomeException;

    QuestionnaireResponse read(FhirContext ctx, IdType theId);

    QuestionnaireResponseEntity readEntity(FhirContext ctx, IdType theId);

    QuestionnaireResponse create(FhirContext ctx, QuestionnaireResponse questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;
    List<QuestionnaireResponse> searchQuestionnaireResponse(FhirContext ctx,

                                            @OptionalParam(name = QuestionnaireResponse.SP_IDENTIFIER) TokenParam identifier,
                                            @OptionalParam(name= QuestionnaireResponse.SP_RES_ID) TokenParam id,
                                            @OptionalParam(name= QuestionnaireResponse.SP_QUESTIONNAIRE) ReferenceParam questionnaire,
                                                            @OptionalParam(name = QuestionnaireResponse.SP_PATIENT) ReferenceParam patient

    );

    List<QuestionnaireResponseEntity> searchQuestionnaireResponseEntity (FhirContext ctx,
                                                                         @OptionalParam(name = QuestionnaireResponse.SP_IDENTIFIER) TokenParam identifier,
                                                                         @OptionalParam(name= QuestionnaireResponse.SP_RES_ID) TokenParam id,
                                                                         @OptionalParam(name= QuestionnaireResponse.SP_QUESTIONNAIRE) ReferenceParam questionnaire,
                                                                         @OptionalParam(name = QuestionnaireResponse.SP_PATIENT) ReferenceParam patient
    );
}
