package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Questionnaire;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireEntity;


import java.util.List;

public interface QuestionnaireRepository extends BaseRepository<QuestionnaireEntity,Questionnaire> {
    void save(FhirContext ctx, QuestionnaireEntity questionnaire) throws OperationOutcomeException;

    Questionnaire read(FhirContext ctx, IdType theId);

    QuestionnaireEntity readEntity(FhirContext ctx, IdType theId);

    Questionnaire create(FhirContext ctx, Questionnaire questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Questionnaire> search(FhirContext ctx,

                    @OptionalParam(name = Questionnaire.SP_IDENTIFIER) TokenParam identifier,
                    @OptionalParam(name= Questionnaire.SP_RES_ID) StringParam id,
                    @OptionalParam(name= Questionnaire.SP_CODE) TokenOrListParam codes,
                    @OptionalParam(name= Questionnaire.SP_URL) UriParam url,
                    @OptionalParam(name= Questionnaire.SP_NAME) StringParam name

    );

    List<QuestionnaireEntity> searchEntity (FhirContext ctx,
                     @OptionalParam(name = Questionnaire.SP_IDENTIFIER) TokenParam identifier,
                     @OptionalParam(name= Questionnaire.SP_RES_ID) StringParam id,
                     @OptionalParam(name= Questionnaire.SP_CODE) TokenOrListParam codes,
                     @OptionalParam(name= Questionnaire.SP_URL) UriParam url,
                     @OptionalParam(name= Questionnaire.SP_NAME) StringParam name


    );
}
