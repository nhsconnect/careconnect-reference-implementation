package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.RiskAssessment;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentEntity;

import java.util.List;

public interface RiskAssessmentRepository extends BaseRepository<RiskAssessmentEntity,RiskAssessment> {
    void save(FhirContext ctx, RiskAssessmentEntity team) throws OperationOutcomeException;

    RiskAssessment read(FhirContext ctx, IdType theId);

    RiskAssessment create(FhirContext ctx, RiskAssessment risk, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    RiskAssessmentEntity readEntity(FhirContext ctx, IdType theId);

    List<RiskAssessment> search(FhirContext ctx,
                          @OptionalParam(name = RiskAssessment.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = RiskAssessment.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = RiskAssessment.SP_RES_ID) StringParam id
    );

    List<RiskAssessmentEntity> searchEntity(FhirContext ctx,
                                      @OptionalParam(name = RiskAssessment.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = RiskAssessment.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = RiskAssessment.SP_RES_ID) StringParam id
    );
}
